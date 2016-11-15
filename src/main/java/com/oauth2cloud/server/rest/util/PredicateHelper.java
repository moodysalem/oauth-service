package com.oauth2cloud.server.rest.util;

import org.apache.commons.lang3.StringUtils;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import java.util.stream.Stream;

import static org.apache.commons.lang3.StringUtils.isBlank;

public abstract class PredicateHelper {

    public static Predicate createSearchPredicate(final CriteriaBuilder cb,
                                                  final String search,
                                                  final Path<String>... paths) {
        if (isBlank(search)) {
            return cb.and();
        }

        if (paths == null || paths.length == 0) {
            return cb.or();
        }

        return cb.and(
                Stream.of(search.split("\\s+"))
                        .filter(StringUtils::isNotBlank)
                        .map(String::trim)
                        .map(str ->
                                cb.or(
                                        Stream.of(paths)
                                                .map(path -> cb.like(path, "%" + str + "%"))
                                                .toArray(Predicate[]::new)
                                )
                        )
                        .toArray(Predicate[]::new)
        );
    }

}
