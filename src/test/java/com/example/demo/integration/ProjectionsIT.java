package com.example.demo.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNull;

import com.example.demo.dto.projection.ProjectionResponse;
import com.example.demo.dto.reservation.ReservationResponse;
import com.example.demo.pageable.Page;
import com.example.demo.repository.model.JMovie;
import com.example.demo.repository.model.JProjection;
import com.example.demo.repository.model.JRoom;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

class ProjectionsIT extends ControllerIT {

  private static final String MANAGER = "MANAGER";
  private static final String CLIENT = "CLIENT";
  private static final String EMPLOYEE = "EMPLOYEE";

  private static String projectionBody(JRoom room, JMovie movie) {
    return String.format(
        "{\"datetime\":\"%s\",\"seatPrice\":9.50,\"roomId\":\"%s\",\"movieId\":\"%s\"}",
        Instant.now().plusSeconds(3600), room.getId(), movie.getId());
  }

  @Test
  void get_projections_returns_200_for_any_authenticated_role() {
    String token = registerAndLogin("proj-client@example.com", CLIENT);
    ResponseEntity<Page<ProjectionResponse>> response =
        get("/projections", token, new ParameterizedTypeReference<Page<ProjectionResponse>>() {});
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();
  }

  @Test
  void get_projections_requires_authentication() {
    ResponseEntity<ProjectionResponse[]> response =
        get("/projections", null, ProjectionResponse[].class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
  }

  @Test
  void manager_can_create_projection() {
    String token = registerAndLogin("proj-manager@example.com", MANAGER);
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
    String token = registerAndLogin("proj-manager-update@example.com", MANAGER);
    JRoom room = saveRoom("R-" + UUID.randomUUID(), 50);
    JMovie movie = saveMovie("Titanic");
    ResponseEntity<ProjectionResponse> created =
        put("/projections", projectionBody(room, movie), token, ProjectionResponse.class);
    String body =
        String.format(
            "{\"id\":\"%s\",\"datetime\":\"%s\",\"seatPrice\":12.00,\"roomId\":\"%s\",\"movieId\":\"%s\"}",
            created.getBody().id(), Instant.now().plusSeconds(7200), room.getId(), movie.getId());
    ResponseEntity<ProjectionResponse> updated =
        put("/projections", body, token, ProjectionResponse.class);
    assertThat(updated.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(updated.getBody().id()).isEqualTo(created.getBody().id());
    assertThat(updated.getBody().seatPrice()).isEqualByComparingTo("12.00");
  }

  @Test
  void list_returns_created_projections() {
    String managerToken = registerAndLogin("proj-manager-list@example.com", MANAGER);
    String clientToken = registerAndLogin("proj-client-list@example.com", CLIENT);
    JRoom room = saveRoom("R-" + UUID.randomUUID(), 50);
    JMovie movie = saveMovie("Joker");
    put("/projections", projectionBody(room, movie), managerToken, ProjectionResponse.class);
    ResponseEntity<Page<ProjectionResponse>> response =
        get("/projections", clientToken, new ParameterizedTypeReference<Page<ProjectionResponse>>() {});
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody().content()).isNotEmpty();
  }

  @Test
  void get_projections_supports_pagination() {
    String managerToken = registerAndLogin("proj-pagination-mgr@example.com", MANAGER);
    String clientToken = registerAndLogin("proj-pagination-client@example.com", CLIENT);
    JRoom room = saveRoom("R-" + UUID.randomUUID(), 50);
    JMovie movie = saveMovie("Pagination-Movie");
    for (int i = 0; i < 3; i++) {
      put("/projections", projectionBody(room, movie), managerToken, ProjectionResponse.class);
    }

    ResponseEntity<Page<ProjectionResponse>> first =
        get("/projections?page=0&pageSize=2", clientToken,
            new ParameterizedTypeReference<Page<ProjectionResponse>>() {});
    assertThat(first.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(first.getBody().content()).hasSize(2);
    assertThat(first.getBody().page()).isZero();
    assertThat(first.getBody().pageSize()).isEqualTo(2);
    assertThat(first.getBody().totalElements()).isGreaterThanOrEqualTo(3);
  }

  @Test
  void get_projection_by_id_returns_200() {
    String managerToken = registerAndLogin("proj-getbyid-mgr@example.com", MANAGER);
    String clientToken = registerAndLogin("proj-getbyid-client@example.com", CLIENT);
    JRoom room = saveRoom("R-" + UUID.randomUUID(), 50);
    JMovie movie = saveMovie("GetById-Proj");
    ResponseEntity<ProjectionResponse> created =
        put("/projections", projectionBody(room, movie), managerToken, ProjectionResponse.class);
    ResponseEntity<ProjectionResponse> response =
        get("/projections/" + created.getBody().id(), clientToken, ProjectionResponse.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody().id()).isEqualTo(created.getBody().id());
  }

  @Test
  void get_projection_by_id_returns_404() {
    String token = registerAndLogin("proj-getbyid-404@example.com", CLIENT);
    ResponseEntity<Map> response = get("/projections/" + UUID.randomUUID(), token, Map.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
  }

  @Test
  void get_projection_by_id_requires_authentication() {
    ResponseEntity<Map> response = get("/projections/" + UUID.randomUUID(), null, Map.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
  }

  @Test
  void manager_can_delete_projection() {
    String token = registerAndLogin("proj-delete@example.com", MANAGER);
    JRoom room = saveRoom("R-" + UUID.randomUUID(), 50);
    JMovie movie = saveMovie("Delete-Proj");
    ResponseEntity<ProjectionResponse> created =
        put("/projections", projectionBody(room, movie), token, ProjectionResponse.class);
    ResponseEntity<Void> response = delete("/projections/" + created.getBody().id(), token, Void.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    ResponseEntity<Map> gone = get("/projections/" + created.getBody().id(), token, Map.class);
    assertThat(gone.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
  }

  @Test
  void client_cannot_delete_projection() {
    String managerToken = registerAndLogin("proj-del-mgr@example.com", MANAGER);
    JRoom room = saveRoom("R-" + UUID.randomUUID(), 50);
    JMovie movie = saveMovie("NoDelete-Proj");
    ResponseEntity<ProjectionResponse> created =
        put("/projections", projectionBody(room, movie), managerToken, ProjectionResponse.class);
    String clientToken = registerAndLogin("proj-del-client@example.com", CLIENT);
    ResponseEntity<Map> response =
        delete("/projections/" + created.getBody().id(), clientToken, Map.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
  }

  @Test
  void employee_cannot_delete_projection() {
    String managerToken = registerAndLogin("proj-del-mgr2@example.com", MANAGER);
    JRoom room = saveRoom("R-" + UUID.randomUUID(), 50);
    JMovie movie = saveMovie("NoDelete-Proj-Emp");
    ResponseEntity<ProjectionResponse> created =
        put("/projections", projectionBody(room, movie), managerToken, ProjectionResponse.class);
    String employeeToken = registerAndLogin("proj-del-emp@example.com", EMPLOYEE);
    ResponseEntity<Map> response =
        delete("/projections/" + created.getBody().id(), employeeToken, Map.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
  }

  @Test
  void delete_projection_not_found_returns_404() {
    String token = registerAndLogin("proj-del-404@example.com", MANAGER);
    ResponseEntity<Map> response = delete("/projections/" + UUID.randomUUID(), token, Map.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
  }

  @Test
  void delete_projection_with_reservations_returns_409() {
    String managerToken = registerAndLogin("proj-del-409-mgr@example.com", MANAGER);
    String clientToken = registerAndLogin("proj-del-409-client@example.com", CLIENT);
    TestData data = seed();
    String reservationBody =
        String.format(
            "{\"projectionId\":\"%s\",\"seatIds\":[\"%s\"]}",
            data.projection().getId(), data.seat().getId());
    put("/reservations", reservationBody, clientToken, ReservationResponse.class);
    ResponseEntity<Map> response =
        delete("/projections/" + data.projection().getId(), managerToken, Map.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
  }

  @Test
  void client_cannot_create_projection() {
    String token = registerAndLogin("proj-client-create@example.com", CLIENT);
    JRoom room = saveRoom("R-" + UUID.randomUUID(), 50);
    JMovie movie = saveMovie("Avatar");
    ResponseEntity<Map> response =
        put("/projections", projectionBody(room, movie), token, Map.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
  }

  @Test
  void create_projection_with_unknown_room_returns_404() {
    String token = registerAndLogin("proj-manager-room@example.com", MANAGER);
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
    String token = registerAndLogin("proj-manager-movie@example.com", MANAGER);
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
    assertNull(projectionMapper.toJProjection(null, jProjection));
  }
}
