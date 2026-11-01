package com.vinsguru.netflux.movie.service;

import com.vinsguru.netflux.movie.dto.MovieDetails;
import com.vinsguru.netflux.movie.dto.MovieSearchRequest;
import com.vinsguru.netflux.movie.dto.MovieSummary;
import com.vinsguru.netflux.movie.entity.Movie;
import com.vinsguru.netflux.movie.exceptions.MovieNotFoundException;
import com.vinsguru.netflux.movie.mapper.MovieMapper;
import com.vinsguru.netflux.movie.repository.MovieRepository;
import com.vinsguru.netflux.movie.repository.MovieSpecifications;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class MovieService {

    private static final int DEFAULT_LIMIT = 20;
    private static final int MAX_LIMIT = 100;

    private static final String VOTE_COUNT = "voteCount";
    private static final String RELEASE_DATE = "releaseDate";
    private static final String ID = "id";

    // nulls last, ties broken by id for deterministic ordering
    private static final Sort MOST_VOTED_SORT = Sort.by(Sort.Order.desc(VOTE_COUNT).nullsLast(), Sort.Order.asc(ID));
    private static final Sort LATEST_SORT = Sort.by(Sort.Order.desc(RELEASE_DATE).nullsLast(), Sort.Order.asc(ID));

    private final MovieRepository repository;

    public MovieService(MovieRepository repository) {
        this.repository = repository;
    }

    public MovieDetails findById(Long id) {
        return repository.findById(id)
                .map(MovieMapper::toDetails)
                .orElseThrow(() -> new MovieNotFoundException(id));
    }

    // de-duplicated, input order kept, unknown IDs skipped
    public List<MovieSummary> findByIds(List<Long> ids) {
        var distinctIds = ids.stream()
                .distinct()
                .toList();
        var moviesById = repository.findAllById(distinctIds)
                .stream()
                .collect(Collectors.toMap(Movie::getId, Function.identity()));
        return distinctIds.stream()
                .filter(moviesById::containsKey)
                .map(moviesById::get)
                .map(MovieMapper::toSummary)
                .toList();
    }

    public List<MovieSummary> findPopular(Integer limit) {
        return findSummaries(Specification.unrestricted(), MOST_VOTED_SORT, limit);
    }

    // future release dates are included
    public List<MovieSummary> findLatest(Integer limit) {
        return findSummaries(Specification.unrestricted(), LATEST_SORT, limit);
    }

    public List<MovieSummary> search(MovieSearchRequest request) {
        var spec = Specification.<Movie>where(MovieSpecifications.hasTitle(request.title()))
                                .and(MovieSpecifications.hasGenre(request.genre()))
                                .and(MovieSpecifications.hasDirector(request.director()));
        return findSummaries(spec, MOST_VOTED_SORT, request.limit());
    }

    // the fluent query applies a limit without the count query Page would run
    private List<MovieSummary> findSummaries(Specification<Movie> spec, Sort sort, Integer limit) {
        List<Movie> movies = repository.findBy(spec, query -> query.sortBy(sort)
                                                                   .limit(resolveLimit(limit))
                                                                   .all());
        return movies.stream()
                .map(MovieMapper::toSummary)
                .toList();
    }

    // missing or below 1 -> default; above max -> max
    private static int resolveLimit(Integer limit) {
        return Objects.isNull(limit) || limit < 1 ? DEFAULT_LIMIT : Math.min(limit, MAX_LIMIT);
    }
}
