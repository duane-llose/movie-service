package com.vinsguru.netflux.movie.dto;

import jakarta.validation.constraints.AssertTrue;
import org.springframework.util.StringUtils;

public record MovieSearchRequest(String title,
                                 String genre,
                                 String director,
                                 Integer limit) {

    // hasText treats blank params as absent
    @AssertTrue(message = "At least one of title, genre or director is required")
    public boolean isAtLeastOneFilterPresent() {
        return StringUtils.hasText(title) || StringUtils.hasText(genre) || StringUtils.hasText(director);
    }
}
