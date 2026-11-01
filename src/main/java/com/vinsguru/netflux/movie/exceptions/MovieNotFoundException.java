package com.vinsguru.netflux.movie.exceptions;

public class MovieNotFoundException extends RuntimeException {

    public MovieNotFoundException(Long id) {
        super("Movie not found: " + id);
    }
}
