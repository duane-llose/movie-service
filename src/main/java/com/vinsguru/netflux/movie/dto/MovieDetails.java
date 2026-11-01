package com.vinsguru.netflux.movie.dto;

import java.time.LocalDate;
import java.util.List;

public record MovieDetails(Long id,
                           String title,
                           String director,
                           Double voteAverage,
                           Integer voteCount,
                           LocalDate releaseDate,
                           Long revenue,
                           Integer runtime,
                           String backdropPath,
                           Long budget,
                           String homepage,
                           String overview,
                           Double popularity,
                           String posterPath,
                           List<String> genres,
                           List<String> themes) {
}
