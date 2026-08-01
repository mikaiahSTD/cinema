package com.example.demo.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNull;

import com.example.demo.constant.Genre;
import com.example.demo.dto.auth.RegisterRequest;
import com.example.demo.dto.movie.MovieResponse;
import com.example.demo.repository.model.JMovie;
import com.example.demo.repository.model.JUser;

import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

class MoviesIT extends ControllerIT {

  @Test
  void get_movies_returns_200_for_any_authenticated_role() {
    String token = registerAndLogin("movie-client@example.com", "CLIENT");
    ResponseEntity<MovieResponse[]> response = get("/movies", token, MovieResponse[].class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
  }

  @Test
  void get_movies_requires_authentication() {
    ResponseEntity<MovieResponse[]> response = get("/movies", null, MovieResponse[].class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
  }

  @Test
  void manager_can_create_movie() {
    String token = registerAndLogin("manager@example.com", "MANAGER");
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
    String token = registerAndLogin("manager-update@example.com", "MANAGER");
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
    String managerToken = registerAndLogin("manager-list@example.com", "MANAGER");
    String clientToken = registerAndLogin("client-list@example.com", "CLIENT");
    put(
        "/movies",
        "{\"title\":\"Inception\",\"genres\":[\"ACTION\"],\"duration\":\"PT2H28M\"}",
        managerToken,
        MovieResponse.class);
    ResponseEntity<MovieResponse[]> response = get("/movies", clientToken, MovieResponse[].class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).hasSizeGreaterThanOrEqualTo(1);
  }

  @Test
  void client_cannot_create_movie() {
    String token = registerAndLogin("client-create@example.com", "CLIENT");
    String body = "{\"title\":\"X\",\"genres\":[\"ACTION\"],\"duration\":\"PT1H\"}";
    ResponseEntity<Map> response = put("/movies", body, token, Map.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
  }

  @Test
  void employee_cannot_create_movie() {
    String token = registerAndLogin("employee-create@example.com", "EMPLOYEE");
    String body = "{\"title\":\"X\",\"genres\":[\"ACTION\"],\"duration\":\"PT1H\"}";
    ResponseEntity<Map> response = put("/movies", body, token, Map.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
  }

  @Test
  void create_movie_with_missing_title_returns_400() {
    String token = registerAndLogin("manager-bad@example.com", "MANAGER");
    String body = "{\"genres\":[\"ACTION\"],\"duration\":\"PT1H\"}";
    ResponseEntity<Map> response = put("/movies", body, token, Map.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
  }

  @Test
  void create_movie_with_invalid_duration_returns_400() {
    String token = registerAndLogin("manager-bad-duration@example.com", "MANAGER");
    String body = "{\"title\":\"X\",\"genres\":[\"ACTION\"],\"duration\":\"not-a-duration\"}";
    ResponseEntity<Map> response = put("/movies", body, token, Map.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
  }
  @Test
        void shouldReturnNullWhenInputIsNull() {
          JMovie jMovie = new JMovie();
            assertNull(movieMapper.toResponse(null));
            assertNull(movieMapper.toJMovie(null,jMovie));
        }
}
