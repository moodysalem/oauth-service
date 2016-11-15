package com.oauth2cloud.server.rest.endpoints.api;

import com.oauth2cloud.server.OAuth2Test;
import com.oauth2cloud.server.model.db.Application;
import com.oauth2cloud.server.model.db.User;
import com.oauth2cloud.server.util.Crud;
import org.testng.annotations.Test;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

public class UsersResourceTest extends OAuth2Test {

    @Test
    public void testQueryParams() {
        final Crud<User> userCrud = userCrud(getToken().getAccessToken());

        userCrud.list(new Crud.Param("grouped", true)).stream().allMatch(u -> u.getGroup() != null);
        userCrud.list(new Crud.Param("grouped", false)).stream().allMatch(u -> u.getGroup() == null);

        userCrud.list(new Crud.Param("applicationId", APPLICATION_ID))
                .stream().allMatch(u -> u.getApplication().getId().equals(APPLICATION_ID));

        // get the application
        final Application application = applicationCrud(getToken().getAccessToken()).get(APPLICATION_ID);

        User u = new User();
        u.setEmail("test-search-me@gmail.com");
        u.setApplication(application);
        final User u1 = userCrud.save(u);

        u.setEmail("should-match-search@gmail.com");
        u.setApplication(application);
        final User u2 = userCrud.save(u);

        final BiFunction<String, Function<List<User>, Boolean>, Boolean> testSearch = (str, checker) -> {
            final List<User> lst = userCrud.list(
                    new Crud.Param("search", str),
                    new Crud.Param("applicationId", APPLICATION_ID)
            );
            return checker.apply(lst);
        };

        final Function<List<User>, Boolean>
                matchesOneOnly =
                list -> list.stream().anyMatch(u1::idMatch) &&
                        list.stream().noneMatch(u2::idMatch),
                matchesTwoOnly =
                        list -> list.stream().anyMatch(u2::idMatch) &&
                                list.stream().noneMatch(u1::idMatch),
                matchesBoth =
                        list -> list.stream().anyMatch(u2::idMatch) &&
                                list.stream().anyMatch(u1::idMatch),
                matchesNeither =
                        list -> list.stream().noneMatch(u2::idMatch) &&
                                list.stream().noneMatch(u1::idMatch);

        testSearch.apply("test-search", matchesOneOnly);
        testSearch.apply("test search", matchesOneOnly);
        testSearch.apply("TEST SEARCH", matchesOneOnly);
        testSearch.apply("test gmail", matchesOneOnly);

        testSearch.apply("should search", matchesTwoOnly);
        testSearch.apply("match should", matchesTwoOnly);
        testSearch.apply("mat sea", matchesTwoOnly);

        testSearch.apply("gmail.com", matchesBoth);
        testSearch.apply("GMAIL.com", matchesBoth);
        testSearch.apply("GMAIL", matchesBoth);
        testSearch.apply("search g", matchesBoth);

        testSearch.apply("matches neither", matchesNeither);
        testSearch.apply("12", matchesNeither);
        testSearch.apply("green", matchesNeither);
    }
}