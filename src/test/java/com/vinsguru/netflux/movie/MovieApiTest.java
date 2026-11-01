package com.vinsguru.netflux.movie;

import com.vinsguru.netflux.movie.dto.MovieDetails;
import com.vinsguru.netflux.movie.dto.MovieSummary;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.ProblemDetail;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.client.RestTestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(TestcontainersConfiguration.class)
@AutoConfigureRestTestClient
@Sql(scripts = "/sql/insert-movies.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "/sql/delete-movies.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class MovieApiTest {

    @Autowired
    RestTestClient restTestClient;

    @Test
    @DisplayName("Should return all fields when the movie exists")
    void shouldReturnMovieDetails() {
        var movie = restTestClient.get()
                .uri("/api/movies/{id}", 1)
                .exchange()
                .expectStatus().isOk()
                .expectBody(MovieDetails.class)
                .returnResult()
                .getResponseBody();

        assertThat(movie).isEqualTo(new MovieDetails(
                1L,
                "The Dark Knight",
                "Christopher Nolan",
                8.5,
                3000,
                LocalDate.of(2008, 7, 18),
                1004558444L,
                152,
                "/dark-knight-backdrop.jpg",
                185000000L,
                "https://example.com/dark-knight",
                "Batman faces the Joker.",
                75.5,
                "/dark-knight-poster.jpg",
                List.of("Action", "Drama"),
                List.of("justice", "chaos")
        ));
    }

    @Test
    @DisplayName("Should return 404 ProblemDetail when the movie does not exist")
    void shouldReturn404WhenMovieNotFound() {
        var problem = restTestClient.get()
                .uri("/api/movies/{id}", 999)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody(ProblemDetail.class)
                .returnResult()
                .getResponseBody();

        assertThat(problem.getStatus()).isEqualTo(404);
        assertThat(problem.getDetail()).isEqualTo("Movie not found: 999");
    }

    @Test
    @DisplayName("Should return 400 ProblemDetail when the movie ID is not a number")
    void shouldReturn400WhenMovieIdInvalid() {
        assertBadRequest("/api/movies/abc");
    }

    @Test
    @DisplayName("Should return batch in input order, de-duplicated and skipping unknown IDs")
    void shouldReturnBatchInInputOrder() {
        assertThat(getIds("/api/movies/batch?ids=7,1,999,1,2")).containsExactly(7L, 1L, 2L);
    }

    @Test
    @DisplayName("Should accept exactly 100 batch IDs")
    void shouldAcceptMaxBatchSize() {
        assertThat(getIds("/api/movies/batch?ids=" + idList(100))).containsExactly(1L, 2L, 3L, 4L, 5L, 6L, 7L);
    }

    @Test
    @DisplayName("Should return 400 ProblemDetail when more than 100 batch IDs are given")
    void shouldReturn400WhenBatchTooLarge() {
        assertBadRequest("/api/movies/batch?ids=" + idList(101));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "/api/movies/batch",
            "/api/movies/batch?ids=",
            "/api/movies/batch?ids=1,x",
            "/api/movies/batch?ids=1,,2"
    })
    @DisplayName("Should return 400 ProblemDetail when batch IDs are missing, empty or invalid")
    void shouldReturn400WhenBatchIdsInvalid(String uri) {
        assertBadRequest(uri);
    }

    @Test
    @DisplayName("Should sort popular by vote count descending, ties by ID, nulls last")
    void shouldSortPopular() {
        assertThat(getIds("/api/movies/popular")).containsExactly(2L, 1L, 3L, 4L, 6L, 7L, 5L);
    }

    @Test
    @DisplayName("Should map summary fields")
    void shouldMapSummaryFields() {
        var movies = getMovies("/api/movies/popular?limit=1");

        assertThat(movies).containsExactly(new MovieSummary(
                2L,
                "Inception",
                "/inception-poster.jpg",
                "/inception-backdrop.jpg",
                8.4,
                LocalDate.of(2010, 7, 16),
                List.of("Action", "Sci-Fi")
        ));
    }

    @Test
    @DisplayName("Should sort latest by release date descending, including future dates, nulls last")
    void shouldSortLatest() {
        assertThat(getIds("/api/movies/latest")).containsExactly(5L, 4L, 3L, 2L, 1L, 7L, 6L);
    }

    @ParameterizedTest
    @ValueSource(strings = {"/api/movies/popular", "/api/movies/latest", "/api/movies/search?title=movie"})
    @Sql(scripts = "/sql/insert-many-movies.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "/sql/delete-movies.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    @DisplayName("Should return 20 results when limit is absent")
    void shouldUseDefaultLimitWhenAbsent(String uri) {
        assertThat(getIds(uri)).hasSize(20);
    }

    @ParameterizedTest
    @ValueSource(strings = {"/api/movies/popular", "/api/movies/latest", "/api/movies/search?title=movie"})
    @Sql(scripts = "/sql/insert-many-movies.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "/sql/delete-movies.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    @DisplayName("Should honor a limit within range")
    void shouldHonorLimitInRange(String uri) {
        assertThat(getIds(withLimit(uri, "5"))).hasSize(5);
    }

    @ParameterizedTest
    @ValueSource(strings = {"/api/movies/popular", "/api/movies/latest", "/api/movies/search?title=movie"})
    @Sql(scripts = "/sql/insert-many-movies.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "/sql/delete-movies.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    @DisplayName("Should clamp a limit above the maximum to 100")
    void shouldClampLimitToMax(String uri) {
        assertThat(getIds(withLimit(uri, "1000"))).hasSize(100);
    }

    @ParameterizedTest
    @ValueSource(strings = {"/api/movies/popular", "/api/movies/latest", "/api/movies/search?title=movie"})
    @Sql(scripts = "/sql/insert-many-movies.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "/sql/delete-movies.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    @DisplayName("Should use the default limit when limit is below 1")
    void shouldUseDefaultLimitWhenBelowOne(String uri) {
        assertThat(getIds(withLimit(uri, "0"))).hasSize(20);
        assertThat(getIds(withLimit(uri, "-1"))).hasSize(20);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "/api/movies/popular?limit=abc",
            "/api/movies/latest?limit=abc",
            "/api/movies/search?title=dark&limit=abc"
    })
    @DisplayName("Should return 400 ProblemDetail when limit is not a number")
    void shouldReturn400WhenLimitInvalid(String uri) {
        assertBadRequest(uri);
    }

    @Test
    @DisplayName("Should match title partially and case-insensitively, sorted by vote count")
    void shouldSearchByTitle() {
        assertThat(getIds("/api/movies/search?title=dark")).containsExactly(1L, 3L);
    }

    @Test
    @DisplayName("Should match director partially and case-insensitively")
    void shouldSearchByDirector() {
        assertThat(getIds("/api/movies/search?director=nolan")).containsExactly(2L, 1L);
    }

    @Test
    @DisplayName("Should match genre exactly")
    void shouldSearchByGenre() {
        assertThat(getIds("/api/movies/search?genre=Action")).containsExactly(2L, 1L, 6L);
    }

    @ParameterizedTest
    @ValueSource(strings = {"action", "Act"})
    @DisplayName("Should not match genre by different case or partial value")
    void shouldNotMatchInexactGenre(String genre) {
        assertThat(getIds("/api/movies/search?genre=" + genre)).isEmpty();
    }

    @Test
    @DisplayName("Should combine search filters with AND logic")
    void shouldCombineFiltersWithAnd() {
        assertThat(getIds("/api/movies/search?title=dark&director=nolan")).containsExactly(1L);
        assertThat(getIds("/api/movies/search?title=dark&genre=Drama&director=haynes")).containsExactly(3L);
    }

    @Test
    @DisplayName("Should treat percent in a search value as a literal character")
    void shouldEscapePercent() {
        assertThat(getIds("/api/movies/search?title={title}", "%")).containsExactly(4L);
    }

    @Test
    @DisplayName("Should treat underscore in a search value as a literal character")
    void shouldEscapeUnderscore() {
        assertThat(getIds("/api/movies/search?title={title}", "_")).containsExactly(7L);
    }

    @Test
    @DisplayName("Should return 400 ProblemDetail when no search filter is given")
    void shouldReturn400WhenNoSearchFilter() {
        assertBadRequest("/api/movies/search");
    }

    @Test
    @DisplayName("Should return 400 ProblemDetail when search filters are blank")
    void shouldReturn400WhenSearchFiltersBlank() {
        assertBadRequest("/api/movies/search?title=");
        assertBadRequest("/api/movies/search?title={title}", " ");
    }

    @Test
    @DisplayName("Should ignore a blank search filter when another is present")
    void shouldIgnoreBlankFilter() {
        assertThat(getIds("/api/movies/search?title=&genre=Drama")).containsExactly(1L, 3L, 5L);
    }

    @Test
    @DisplayName("Should return 405 ProblemDetail with Allow header when the method is not supported")
    void shouldReturn405WhenMethodNotSupported() {
        var problem = restTestClient.post()
                .uri("/api/movies/{id}", 1)
                .exchange()
                .expectStatus().isEqualTo(405)
                .expectHeader().exists("Allow")
                .expectBody(ProblemDetail.class)
                .returnResult()
                .getResponseBody();

        assertThat(problem.getStatus()).isEqualTo(405);
    }

    @Test
    @DisplayName("Should return 404 ProblemDetail when the path is not mapped")
    void shouldReturn404WhenPathNotMapped() {
        var problem = restTestClient.get()
                .uri("/api/unknown")
                .exchange()
                .expectStatus().isNotFound()
                .expectBody(ProblemDetail.class)
                .returnResult()
                .getResponseBody();

        assertThat(problem.getStatus()).isEqualTo(404);
    }

    @Test
    @DisplayName("Should return 500 ProblemDetail without internal details when the database rejects a query")
    void shouldReturn500WithoutInternalDetails() {
        // PostgreSQL rejects a null byte in a text value, a real failure that needs no mocking
        var problem = restTestClient.get()
                .uri("/api/movies/search?title={title}", "\u0000")
                .exchange()
                .expectStatus().is5xxServerError()
                .expectBody(ProblemDetail.class)
                .returnResult()
                .getResponseBody();

        assertThat(problem.getStatus()).isEqualTo(500);
        assertThat(problem.getDetail()).isEqualTo("An unexpected error occurred");
    }

    private List<MovieSummary> getMovies(String uri, Object... uriVariables) {
        return restTestClient.get()
                .uri(uri, uriVariables)
                .exchange()
                .expectStatus().isOk()
                .expectBody(new ParameterizedTypeReference<List<MovieSummary>>() {})
                .returnResult()
                .getResponseBody();
    }

    private List<Long> getIds(String uri, Object... uriVariables) {
        return getMovies(uri, uriVariables).stream()
                .map(MovieSummary::id)
                .toList();
    }

    private void assertBadRequest(String uri, Object... uriVariables) {
        var problem = restTestClient.get()
                .uri(uri, uriVariables)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody(ProblemDetail.class)
                .returnResult()
                .getResponseBody();

        assertThat(problem.getStatus()).isEqualTo(400);
        assertThat(problem.getTitle()).isEqualTo("Bad Request");
    }

    private static String withLimit(String uri, String limit) {
        return UriComponentsBuilder.fromUriString(uri)
                .queryParam("limit", limit)
                .toUriString();
    }

    private static String idList(int size) {
        return LongStream.rangeClosed(1, size)
                .mapToObj(Long::toString)
                .collect(Collectors.joining(","));
    }
}
