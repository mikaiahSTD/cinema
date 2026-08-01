package com.example.demo.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNull;

import com.example.demo.dto.projection.ProjectionResponse;
import com.example.demo.repository.model.JMovie;
import com.example.demo.repository.model.JProjection;
import com.example.demo.repository.model.JRoom;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

class ProjectionsIT extends ControllerIT {

  private static String projectionBody(JRoom room, JMovie movie) {
    return String.format(
        "{\"datetime\":\"%s\",\"seatPrice\":9.50,\"roomId\":\"%s\",\"movieId\":\"%s\"}",
        Instant.now().plusSeconds(3600), room.getId(), movie.getId());
  }

  @Test
  void get_projections_returns_200_for_any_authenticated_role() {
    String token = registerAndLogin("proj-client@example.com", "CLIENT");
    ResponseEntity<ProjectionResponse[]> response =
        get("/projections", token, ProjectionResponse[].class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
  }

  @Test
  void get_projections_requires_authentication() {
    ResponseEntity<ProjectionResponse[]> response =
        get("/projections", null, ProjectionResponse[].class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
  }

  @Test
  void manager_can_create_projection() {
    String token = registerAndLogin("proj-manager@example.com", "MANAGER");
    JRoom room = saveRoom("R-" + UUID.randomUUID(), 50);
    JMovie movie = saveMovie("Inception");
    ResponseEntity<ProjectionResponse> response =
        put("/projections", projectionBody(room, movie), token, ProjectionResponse.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().id()).isNotNull();
    assertThat(response.getBody().roomId()).isEqualTo(room.getId());
    assertThat(response.getBody().movieId()).isEqualTo(movie.getId());
    assertThat(response.getBody().seatPrice()).isEqualByComparingTo("9.50");
  }

  @Test
  void manager_can_update_projection() {
    String token = registerAndLogin("proj-manager-update@example.com", "MANAGER");
    JRoom room = saveRoom("R-" + UUID.randomUUID(), 50);
    JMovie movie = saveMovie("Titanic");
    ResponseEntity<ProjectionResponse> created =
        put("/projections", projectionBody(room, movie), token, ProjectionResponse.class);
    String body =
        String.format(
            "{\"id\":\"%s\",\"datetime\":\"%s\",\"seatPrice\":12.00,\"roomId\":\"%s\",\"movieId\":\"%s\"}",
            created.getBody().id(),
            Instant.now().plusSeconds(7200),
            room.getId(),
            movie.getId());
    ResponseEntity<ProjectionResponse> updated =
        put("/projections", body, token, ProjectionResponse.class);
    assertThat(updated.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(updated.getBody().id()).isEqualTo(created.getBody().id());
    assertThat(updated.getBody().seatPrice()).isEqualByComparingTo("12.00");
  }

  @Test
  void list_returns_created_projections() {
    String managerToken = registerAndLogin("proj-manager-list@example.com", "MANAGER");
    String clientToken = registerAndLogin("proj-client-list@example.com", "CLIENT");
    JRoom room = saveRoom("R-" + UUID.randomUUID(), 50);
    JMovie movie = saveMovie("Joker");
    put("/projections", projectionBody(room, movie), managerToken, ProjectionResponse.class);
    ResponseEntity<ProjectionResponse[]> response =
        get("/projections", clientToken, ProjectionResponse[].class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).hasSizeGreaterThanOrEqualTo(1);
  }

  @Test
  void client_cannot_create_projection() {
    String token = registerAndLogin("proj-client-create@example.com", "CLIENT");
    JRoom room = saveRoom("R-" + UUID.randomUUID(), 50);
    JMovie movie = saveMovie("Avatar");
    ResponseEntity<Map> response = put("/projections", projectionBody(room, movie), token, Map.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
  }

  @Test
  void create_projection_with_unknown_room_returns_404() {
    String token = registerAndLogin("proj-manager-room@example.com", "MANAGER");
    JMovie movie = saveMovie("Dune");
    String body =
        String.format(
            "{\"datetime\":\"%s\",\"seatPrice\":9.50,\"roomId\":\"%s\",\"movieId\":\"%s\"}",
            Instant.now().plusSeconds(3600), UUID.randomUUID(), movie.getId());
    ResponseEntity<Map> response = put("/projections", body, token, Map.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
  }

  @Test
  void create_projection_with_missing_movie_returns_400() {
    String token = registerAndLogin("proj-manager-movie@example.com", "MANAGER");
    JRoom room = saveRoom("R-" + UUID.randomUUID(), 50);
    String body =
        String.format(
            "{\"datetime\":\"%s\",\"seatPrice\":9.50,\"roomId\":\"%s\"}",
            Instant.now().plusSeconds(3600), room.getId());
    ResponseEntity<Map> response = put("/projections", body, token, Map.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
  }
  @Test
        void shouldReturnNullWhenInputIsNull() {
          JProjection jProjection = new JProjection();
            assertNull(projectionMapper.toResponse(null));
            assertNull(projectionMapper.toJProjection(null,jProjection));
        }
}
