package com.oauth2cloud.server.rest;

import com.moodysalem.jaxrs.lib.factories.JAXRSEntityManagerFactory;
import com.moodysalem.jaxrs.lib.factories.MailSessionFactory;
import freemarker.cache.ClassTemplateLoader;
import freemarker.template.Configuration;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.process.internal.RequestScoped;

import javax.mail.Session;
import javax.persistence.EntityManager;

/**
 * Registers a factory for entity managers and sending e-mails and parsing freemarker
 * templates
 */
public class ResourceBinder extends AbstractBinder {
    @Override
    protected void configure() {

    }
}
