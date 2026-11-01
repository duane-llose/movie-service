package com.vinsguru.netflux.movie.repository;

import com.vinsguru.netflux.movie.entity.Movie;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.Locale;

public class MovieSpecifications {

    private static final char LIKE_ESCAPE = '\\';

    public static Specification<Movie> hasTitle(String title) {
        return containsIgnoreCase("title", title);
    }

    public static Specification<Movie> hasDirector(String director) {
        return containsIgnoreCase("director", director);
    }

    // exact, case-sensitive array membership (analysis #4)
    public static Specification<Movie> hasGenre(String genre) {
        return (root, query, cb) ->
                !StringUtils.hasText(genre) ? cb.conjunction() :
                cb.greaterThan(cb.function("array_position", Integer.class, root.get("genres"), cb.literal(genre)), 0);
    }

    private static Specification<Movie> containsIgnoreCase(String attribute, String value) {
        return (root, query, cb) ->
                !StringUtils.hasText(value) ? cb.conjunction() :
                cb.like(cb.lower(root.get(attribute)), "%" + escapeLike(value).toLowerCase(Locale.ROOT) + "%", LIKE_ESCAPE);
    }

    // analysis #18
    private static String escapeLike(String value) {
        return value.replace("\\", "\\\\")
                    .replace("%", "\\%")
                    .replace("_", "\\_");
    }
}
