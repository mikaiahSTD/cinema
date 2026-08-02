package com.example.demo.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNull;

import com.example.demo.constant.ReservationStatus;
import com.example.demo.dto.reservation.ReservationResponse;
import com.example.demo.pageable.Page;
import com.example.demo.repository.model.JReservation;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

class ReservationsIT extends ControllerIT {

  private static final String CLIENT = "CLIENT";
  private static final String EMPLOYEE = "EMPLOYEE";
  private static final String MANAGER = "MANAGER";

  private static String reservationBody(TestData data, String status) {
    String statusPart = status == null ? "" : ",\"status\":\"" + status + "\"";
    return String.format(
        "{\"projectionId\":\"%s\",\"seatIds\":[\"%s\"]%s}",
        data.projection().getId(), data.seat().getId(), statusPart);
  }

  private static String updateBody(TestData data, UUID reservationId, String status) {
    String statusPart = status == null ? "" : ",\"status\":\"" + status + "\"";
    return String.format(
        "{\"id\":\"%s\",\"projectionId\":\"%s\",\"seatIds\":[\"%s\"]%s}",
        reservationId, data.projection().getId(), data.seat().getId(), statusPart);
  }

  @Test
  void client_can_create_own_pending_reservation() {
    String token = registerAndLogin("res-client@example.com", "CLIENT");
    TestData data = seed();
    ResponseEntity<ReservationResponse> response =
        put("/reservations", reservationBody(data, null), token, ReservationResponse.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().id()).isNotNull();
    assertThat(response.getBody().status()).isEqualTo(ReservationStatus.PENDING);
    assertThat(response.getBody().seatIds()).containsExactly(data.seat().getId());
  }

  @Test
  void client_can_fetch_own_reservation() {
    String token = registerAndLogin("res-client-own@example.com", "CLIENT");
    TestData data = seed();
    ResponseEntity<ReservationResponse> created =
        put("/reservations", reservationBody(data, null), token, ReservationResponse.class);
    ResponseEntity<ReservationResponse> fetched =
        get("/reservations/" + created.getBody().id(), token, ReservationResponse.class);
    assertThat(fetched.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(fetched.getBody().id()).isEqualTo(created.getBody().id());
  }

  @Test
  void client_cannot_fetch_other_clients_reservation() {
    String tokenA = registerAndLogin("res-client-a@example.com", "CLIENT");
    String tokenB = registerAndLogin("res-client-b@example.com", "CLIENT");
    TestData data = seed();
    ResponseEntity<ReservationResponse> created =
        put("/reservations", reservationBody(data, null), tokenA, ReservationResponse.class);
    ResponseEntity<Map> fetched = get("/reservations/" + created.getBody().id(), tokenB, Map.class);
    assertThat(fetched.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
  }

  @Test
  void employee_can_fetch_any_reservation() {
    String clientToken = registerAndLogin("res-client-emp@example.com", "CLIENT");
    String employeeToken = registerAndLogin("res-employee@example.com", "EMPLOYEE");
    TestData data = seed();
    ResponseEntity<ReservationResponse> created =
        put("/reservations", reservationBody(data, null), clientToken, ReservationResponse.class);
    ResponseEntity<ReservationResponse> fetched =
        get("/reservations/" + created.getBody().id(), employeeToken, ReservationResponse.class);
    assertThat(fetched.getStatusCode()).isEqualTo(HttpStatus.OK);
  }

  @Test
  void employee_can_list_all_reservations() {
    String clientToken = registerAndLogin("res-client-list@example.com", CLIENT);
    String employeeToken = registerAndLogin("res-employee-list@example.com", EMPLOYEE);
    TestData data = seed();
    put("/reservations", reservationBody(data, null), clientToken, ReservationResponse.class);
    ResponseEntity<Page<ReservationResponse>> response =
        get(
            "/reservations",
            employeeToken,
            new ParameterizedTypeReference<Page<ReservationResponse>>() {});
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().content()).hasSizeGreaterThanOrEqualTo(1);
  }

  @Test
  void get_reservations_supports_pagination() {
    String clientToken = registerAndLogin("res-pagination-client@example.com", CLIENT);
    String employeeToken = registerAndLogin("res-pagination-emp@example.com", EMPLOYEE);
    TestData data = seed();
    TestData data2 = seed();
    TestData data3 = seed();
    put("/reservations", reservationBody(data, null), clientToken, ReservationResponse.class);
    put("/reservations", reservationBody(data2, null), clientToken, ReservationResponse.class);
    put("/reservations", reservationBody(data3, null), clientToken, ReservationResponse.class);

    ResponseEntity<Page<ReservationResponse>> response =
        get(
            "/reservations?page=0&pageSize=2",
            employeeToken,
            new ParameterizedTypeReference<Page<ReservationResponse>>() {});
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody().content()).hasSize(2);
    assertThat(response.getBody().page()).isZero();
    assertThat(response.getBody().pageSize()).isEqualTo(2);
    assertThat(response.getBody().totalElements()).isGreaterThanOrEqualTo(3);
  }

  @Test
  void manager_can_delete_reservation() {
    String clientToken = registerAndLogin("res-delete-client@example.com", CLIENT);
    String managerToken = registerAndLogin("res-delete-mgr@example.com", MANAGER);
    TestData data = seed();
    ResponseEntity<ReservationResponse> created =
        put("/reservations", reservationBody(data, null), clientToken, ReservationResponse.class);
    ResponseEntity<Void> response =
        delete("/reservations/" + created.getBody().id(), managerToken, Void.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    ResponseEntity<Map> gone =
        get("/reservations/" + created.getBody().id(), managerToken, Map.class);
    assertThat(gone.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
  }

  @Test
  void client_cannot_delete_reservation() {
    String clientToken = registerAndLogin("res-del-client@example.com", CLIENT);
    String managerToken = registerAndLogin("res-del-mgr2@example.com", MANAGER);
    TestData data = seed();
    ResponseEntity<ReservationResponse> created =
        put("/reservations", reservationBody(data, null), clientToken, ReservationResponse.class);
    ResponseEntity<Map> response =
        delete("/reservations/" + created.getBody().id(), clientToken, Map.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    assertThat(
            get("/reservations/" + created.getBody().id(), managerToken, Map.class).getStatusCode())
        .isEqualTo(HttpStatus.OK);
  }

  @Test
  void employee_cannot_delete_reservation() {
    String clientToken = registerAndLogin("res-del-emp-client@example.com", CLIENT);
    String employeeToken = registerAndLogin("res-del-emp@example.com", EMPLOYEE);
    TestData data = seed();
    ResponseEntity<ReservationResponse> created =
        put("/reservations", reservationBody(data, null), clientToken, ReservationResponse.class);
    ResponseEntity<Map> response =
        delete("/reservations/" + created.getBody().id(), employeeToken, Map.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
  }

  @Test
  void delete_reservation_not_found_returns_404() {
    String token = registerAndLogin("res-del-404@example.com", MANAGER);
    ResponseEntity<Map> response = delete("/reservations/" + UUID.randomUUID(), token, Map.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
  }

  @Test
  void client_cannot_list_all_reservations() {
    String token = registerAndLogin("res-client-forbidden@example.com", "CLIENT");
    ResponseEntity<Map> response = get("/reservations", token, Map.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
  }

  @Test
  void client_cannot_validate_reservation() {
    String token = registerAndLogin("res-client-validate@example.com", "CLIENT");
    TestData data = seed();
    ResponseEntity<Map> response =
        put("/reservations", reservationBody(data, "SUCCESS"), token, Map.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
  }

  @Test
  void client_cannot_cancel_reservation() {
    String token = registerAndLogin("res-client-cancel@example.com", "CLIENT");
    TestData data = seed();
    ResponseEntity<Map> response =
        put("/reservations", reservationBody(data, "CANCELED"), token, Map.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
  }

  @Test
  void employee_can_validate_reservation() {
    String token = registerAndLogin("res-employee-validate@example.com", "EMPLOYEE");
    TestData data = seed();
    ResponseEntity<ReservationResponse> response =
        put("/reservations", reservationBody(data, "SUCCESS"), token, ReservationResponse.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody().status()).isEqualTo(ReservationStatus.SUCCESS);
  }

  @Test
  void client_cannot_modify_other_clients_reservation() {
    String tokenA = registerAndLogin("res-client-mod-a@example.com", "CLIENT");
    String tokenB = registerAndLogin("res-client-mod-b@example.com", "CLIENT");
    TestData data = seed();
    ResponseEntity<ReservationResponse> created =
        put("/reservations", reservationBody(data, null), tokenA, ReservationResponse.class);
    ResponseEntity<Map> response =
        put("/reservations", updateBody(data, created.getBody().id(), null), tokenB, Map.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
  }

  @Test
  void employee_can_cancel_clients_reservation() {
    String clientToken = registerAndLogin("res-client-cancel2@example.com", "CLIENT");
    String employeeToken = registerAndLogin("res-employee-cancel@example.com", "EMPLOYEE");
    TestData data = seed();
    ResponseEntity<ReservationResponse> created =
        put("/reservations", reservationBody(data, null), clientToken, ReservationResponse.class);
    ResponseEntity<ReservationResponse> response =
        put(
            "/reservations",
            updateBody(data, created.getBody().id(), "CANCELED"),
            employeeToken,
            ReservationResponse.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody().status()).isEqualTo(ReservationStatus.CANCELED);
  }

  @Test
  void get_unknown_reservation_returns_404() {
    String token = registerAndLogin("res-employee-404@example.com", "EMPLOYEE");
    ResponseEntity<Map> response = get("/reservations/" + UUID.randomUUID(), token, Map.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
  }

  @Test
  void create_reservation_with_missing_projection_returns_400() {
    String token = registerAndLogin("res-client-400@example.com", "CLIENT");
    TestData data = seed();
    ResponseEntity<Map> response =
        put("/reservations", "{\"seatIds\":[\"" + data.seat().getId() + "\"]}", token, Map.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
  }

  @Test
  void create_reservation_with_unknown_projection_returns_404() {
    String token = registerAndLogin("res-client-404@example.com", "CLIENT");
    TestData data = seed();
    String body =
        "{\"projectionId\":\""
            + UUID.randomUUID()
            + "\",\"seatIds\":[\""
            + data.seat().getId()
            + "\"]}";
    ResponseEntity<Map> response = put("/reservations", body, token, Map.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
  }

  @Test
  void shouldReturnNullWhenInputIsNull() {
    JReservation jReservation = new JReservation();
    assertNull(reservationMapper.toResponse(null));
    assertNull(reservationMapper.toJReservation(null, jReservation, UUID.randomUUID()));
  }
}
