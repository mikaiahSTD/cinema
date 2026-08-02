package com.example.demo.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNull;

import com.example.demo.constant.Genre;
import com.example.demo.dto.movie.MovieResponse;
import com.example.demo.pageable.Page;
import com.example.demo.repository.model.JMovie;
import com.example.demo.repository.model.JRoom;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

class MoviesIT extends ControllerIT {

  private static final String MANAGER = "MANAGER";
  private static final String CLIENT = "CLIENT";
  private static final String EMPLOYEE = "EMPLOYEE";

  private static String movieBody(String title) {
    return "{\"title\":\"" + title + "\",\"genres\":[\"ACTION\"],\"duration\":\"PT1H\"}";
  }

  private String createMovie(String title) {
    String token = registerAndLogin("movies-manager-" + System.nanoTime() + "@example.com", MANAGER);
    return put("/movies", movieBody(title), token, MovieResponse.class).getBody().id().toString();
  }

  @Test
  void get_movies_returns_200_for_any_authenticated_role() {
    String token = registerAndLogin("movie-client@example.com", CLIENT);
    ResponseEntity<Page<MovieResponse>> response =
        get("/movies", token, new ParameterizedTypeReference<Page<MovieResponse>>() {});
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();
  }

  @Test
  void get_movies_requires_authentication() {
    ResponseEntity<MovieResponse[]> response = get("/movies", null, MovieResponse[].class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
  }

  @Test
  void manager_can_create_movie() {
    String token = registerAndLogin("manager@example.com", MANAGER);
    String body =
        "{\"title\":\"Inception\",\"genres\":[\"ACTION\",\"SCI_FI\"],"
            + "\"description\":\"A thief steals secrets through dreams\",\"duration\":\"PT2H28M\"}";
    ResponseEntity<MovieResponse> response = put("/movies", body, token, MovieResponse.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().id()).isNotNull();
    assertThat(response.getBody().title()).isEqualTo("Inception");
    assertThat(response.getBody().genres()).containsExactly(Genre.ACTION, Genre.SCI_FI);
    assertThat(response.getBody().duration()).isEqualTo("PT2H28M");
  }

  @Test
  void manager_can_update_movie() {
    String token = registerAndLogin("manager-update@example.com", MANAGER);
    ResponseEntity<MovieResponse> created =
        put(
            "/movies",
            "{\"title\":\"Old\",\"genres\":[\"ACTION\"],\"duration\":\"PT1H\"}",
            token,
            MovieResponse.class);
    String body =
        "{\"id\":\""
            + created.getBody().id()
            + "\",\"title\":\"New\",\"genres\":[\"DRAMA\"],\"duration\":\"PT1H30M\"}";
    ResponseEntity<MovieResponse> updated = put("/movies", body, token, MovieResponse.class);
    assertThat(updated.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(updated.getBody().title()).isEqualTo("New");
    assertThat(updated.getBody().genres()).containsExactly(Genre.DRAMA);
    assertThat(updated.getBody().duration()).isEqualTo("PT1H30M");
  }

  @Test
  void list_returns_created_movies() {
    String managerToken = registerAndLogin("manager-list@example.com", MANAGER);
    String clientToken = registerAndLogin("client-list@example.com", CLIENT);
    put(
        "/movies",
        "{\"title\":\"Inception\",\"genres\":[\"ACTION\"],\"duration\":\"PT2H28M\"}",
        managerToken,
        MovieResponse.class);
    ResponseEntity<Page<MovieResponse>> response =
        get("/movies", clientToken, new ParameterizedTypeReference<Page<MovieResponse>>() {});
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody().content()).isNotEmpty();
  }

  @Test
  void get_movies_supports_pagination() {
    String token = registerAndLogin("movies-pagination@example.com", CLIENT);
    createMovie("Page-Movie-A");
    createMovie("Page-Movie-B");
    createMovie("Page-Movie-C");

    ResponseEntity<Page<MovieResponse>> first =
        get("/movies?page=0&pageSize=2", token, new ParameterizedTypeReference<Page<MovieResponse>>() {});
    assertThat(first.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(first.getBody().content()).hasSize(2);
    assertThat(first.getBody().page()).isZero();
    assertThat(first.getBody().pageSize()).isEqualTo(2);
    assertThat(first.getBody().totalElements()).isGreaterThanOrEqualTo(3);
    assertThat(first.getBody().totalPages()).isGreaterThanOrEqualTo(2);

    ResponseEntity<Page<MovieResponse>> second =
        get("/movies?page=1&pageSize=2", token, new ParameterizedTypeReference<Page<MovieResponse>>() {});
    assertThat(second.getBody().content()).isNotEmpty();
    assertThat(second.getBody().page()).isEqualTo(1);
  }

  @Test
  void get_movie_by_id_returns_200() {
    String token = registerAndLogin("movies-getbyid@example.com", CLIENT);
    String id = createMovie("GetById-Movie");
    ResponseEntity<MovieResponse> response = get("/movies/" + id, token, MovieResponse.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody().id().toString()).isEqualTo(id);
    assertThat(response.getBody().title()).isEqualTo("GetById-Movie");
  }

  @Test
  void get_movie_by_id_returns_404() {
    String token = registerAndLogin("movies-getbyid-404@example.com", CLIENT);
    ResponseEntity<Map> response =
        get("/movies/" + UUID.randomUUID(), token, Map.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
  }

  @Test
  void get_movie_by_id_requires_authentication() {
    ResponseEntity<Map> response =
        get("/movies/" + UUID.randomUUID(), null, Map.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
  }

  @Test
  void manager_can_delete_movie() {
    String token = registerAndLogin("movies-delete@example.com", MANAGER);
    String id = createMovie("Delete-Movie");
    ResponseEntity<Void> response = delete("/movies/" + id, token, Void.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    ResponseEntity<Map> gone = get("/movies/" + id, token, Map.class);
    assertThat(gone.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
  }

  @Test
  void client_cannot_delete_movie() {
    String managerToken = registerAndLogin("movies-del-mgr@example.com", MANAGER);
    String id = createMovie("NoDelete-Client");
    String clientToken = registerAndLogin("movies-del-client@example.com", CLIENT);
    ResponseEntity<Map> response = delete("/movies/" + id, clientToken, Map.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    assertThat(get("/movies/" + id, managerToken, MovieResponse.class).getStatusCode())
        .isEqualTo(HttpStatus.OK);
  }

  @Test
  void employee_cannot_delete_movie() {
    String id = createMovie("NoDelete-Employee");
    String employeeToken = registerAndLogin("movies-del-emp@example.com", EMPLOYEE);
    ResponseEntity<Map> response = delete("/movies/" + id, employeeToken, Map.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
  }

  @Test
  void delete_movie_not_found_returns_404() {
    String token = registerAndLogin("movies-del-404@example.com", MANAGER);
    ResponseEntity<Map> response =
        delete("/movies/" + UUID.randomUUID(), token, Map.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
  }

  @Test
  void delete_movie_with_projections_returns_409() {
    String token = registerAndLogin("movies-del-409@example.com", MANAGER);
    String id = createMovie("Keep-Movie");
    JRoom room = saveRoom("R-" + UUID.randomUUID(), 50);
    saveProjection(room, movieRepository.findById(UUID.fromString(id)).orElseThrow());
    ResponseEntity<Map> response = delete("/movies/" + id, token, Map.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
  }

  @Test
  void client_cannot_create_movie() {
    String token = registerAndLogin("client-create@example.com", CLIENT);
    String body = "{\"title\":\"X\",\"genres\":[\"ACTION\"],\"duration\":\"PT1H\"}";
    ResponseEntity<Map> response = put("/movies", body, token, Map.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
  }

  @Test
  void employee_cannot_create_movie() {
    String token = registerAndLogin("employee-create@example.com", EMPLOYEE);
    String body = "{\"title\":\"X\",\"genres\":[\"ACTION\"],\"duration\":\"PT1H\"}";
    ResponseEntity<Map> response = put("/movies", body, token, Map.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
  }

  @Test
  void create_movie_with_missing_title_returns_400() {
    String token = registerAndLogin("manager-bad@example.com", MANAGER);
    String body = "{\"genres\":[\"ACTION\"],\"duration\":\"PT1H\"}";
    ResponseEntity<Map> response = put("/movies", body, token, Map.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
  }

  @Test
  void create_movie_with_invalid_duration_returns_400() {
    String token = registerAndLogin("manager-bad-duration@example.com", MANAGER);
    String body = "{\"title\":\"X\",\"genres\":[\"ACTION\"],\"duration\":\"not-a-duration\"}";
    ResponseEntity<Map> response = put("/movies", body, token, Map.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
  }

  @Test
  void shouldReturnNullWhenInputIsNull() {
    JMovie jMovie = new JMovie();
    assertNull(movieMapper.toResponse(null));
    assertNull(movieMapper.toJMovie(null, jMovie));
  }
}
