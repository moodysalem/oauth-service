package com.leaguekit.oauth.factories;

import liquibase.Liquibase;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.LiquibaseException;
import liquibase.resource.ClassLoaderResourceAccessor;
import org.glassfish.hk2.api.Factory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.persistence.Persistence;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.util.TimeZone;

public class EntityManagerFactory implements Factory<EntityManager> {

    private String url;
    private String user;
    private String password;
    private boolean showSql;

    public EntityManagerFactory(String url, String user, String password, boolean showSql) {
        this.url = url;
        this.user = user;
        this.password = password;
        this.showSql = showSql;
        runMigrations();
        _emf = createEMF();
    }

    private static final Logger LOG = LoggerFactory.getLogger(EntityManagerFactory.class);
    private static final String[] DRIVERS = new String[]{"com.mysql.jdbc.Driver", "org.postgresql.Driver",
        "oracle.jdbc.driver.OracleDriver", "com.microsoft.sqlserver.jdbc.SQLServerDriver"};

    private javax.persistence.EntityManagerFactory _emf;

    static {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        loadDrivers();
    }

    private static void loadDrivers() {
        for (String driverName : DRIVERS) {
            try {
                Class.forName(driverName);
                LOG.info("JDBC Driver loaded: " + driverName);
            } catch (ClassNotFoundException e) {
                LOG.error("JDBC Driver not found in classpath. This is benign if the driver is not needed: " + driverName);
            }
        }
    }

    private javax.persistence.EntityManagerFactory createEMF() {
        Properties properties = new Properties();
        properties.setProperty("hibernate.connection.url", url);
        properties.setProperty("hibernate.connection.user", user);
        properties.setProperty("hibernate.connection.password", password);
        properties.setProperty("hibernate.connection.useUnicode", "true");
        properties.setProperty("hibernate.hbm2ddl.auto", "validate");

        if (showSql) {
            properties.setProperty("hibernate.show_sql", "true");
            properties.setProperty("hibernate.format_sql", "true");
        }

        // database connection pool
        properties.setProperty("hibernate.c3p0.min_size", "1");
        properties.setProperty("hibernate.c3p0.max_size", "100");
        properties.setProperty("hibernate.c3p0.idle_test_period", "1000");
        properties.setProperty("hibernate.c3p0.timeout", "100");
        properties.setProperty("hibernate.c3p0.max_statements", "50");
        properties.setProperty("hibernate.default_batch_fetch_size", "32");

        return Persistence.createEntityManagerFactory("leaguekit-hibernate", properties);
    }

    private void runMigrations() {
        try (Connection c = DriverManager.getConnection(url, user, password)) {
            LOG.info("Running Migrations");
            // first run the liquibase migrations against the database
            Liquibase lb = new Liquibase("db/master-changelog.xml", new ClassLoaderResourceAccessor(), new JdbcConnection(c));
            lb.update((showSql) ? "test" : "prod");
        } catch (LiquibaseException e) {
            LOG.error("Liquibase exception thrown while trying to run migrations", e);
        } catch (SQLException e) {
            LOG.error("SQL Exception thrown while trying to open a connection", e);
        }
    }

    @Override
    public EntityManager provide() {
        LOG.info("Providing an entity manager");
        return _emf.createEntityManager();
    }

    @Override
    public void dispose(EntityManager entityManager) {
        LOG.info("Disposing an entity manager");
        entityManager.close();
    }
}
