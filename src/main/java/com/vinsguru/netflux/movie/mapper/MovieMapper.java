package com.vinsguru.netflux.movie.mapper;

import com.vinsguru.netflux.movie.dto.MovieDetails;
import com.vinsguru.netflux.movie.dto.MovieSummary;
import com.vinsguru.netflux.movie.entity.Movie;

public class MovieMapper {

    public static MovieSummary toSummary(Movie movie) {
        return new MovieSummary(
                movie.getId(),
                movie.getTitle(),
                movie.getPosterPath(),
                movie.getBackdropPath(),
                movie.getVoteAverage(),
                movie.getReleaseDate(),
                movie.getGenres()
        );
    }

    public static MovieDetails toDetails(Movie movie) {
        return new MovieDetails(
                movie.getId(),
                movie.getTitle(),
                movie.getDirector(),
                movie.getVoteAverage(),
                movie.getVoteCount(),
                movie.getReleaseDate(),
                movie.getRevenue(),
                movie.getRuntime(),
                movie.getBackdropPath(),
                movie.getBudget(),
                movie.getHomepage(),
                movie.getOverview(),
                movie.getPopularity(),
                movie.getPosterPath(),
                movie.getGenres(),
                movie.getThemes()
        );
    }
}
