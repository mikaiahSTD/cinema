package com.example.demo.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNull;

import com.example.demo.constant.ReservationStatus;
import com.example.demo.dto.reservation.ReservationResponse;
import com.example.demo.pageable.Page;
import com.example.demo.repository.model.JMovie;
import com.example.demo.repository.model.JProjection;
import com.example.demo.repository.model.JReservation;
import com.example.demo.repository.model.JRoom;
import com.example.demo.repository.model.JSeat;
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

  private static String reservationBody(JProjection projection, JSeat seat) {
    return String.format(
        "{\"projectionId\":\"%s\",\"seatIds\":[\"%s\"]}", projection.getId(), seat.getId());
  }

  private static String reservationBody(JProjection projection, JSeat seat, String status) {
    String statusPart = status == null ? "" : ",\"status\":\"" + status + "\"";
    return String.format(
        "{\"projectionId\":\"%s\",\"seatIds\":[\"%s\"]%s}",
        projection.getId(), seat.getId(), statusPart);
  }

  private record SeededData(
      JRoom room, JSeat seatA, JSeat seatB, JMovie movie, JProjection projection) {}

  private SeededData seedWithCapacity(int capacity) {
    JRoom room = saveRoom("R-cap-" + UUID.randomUUID(), capacity);
    JSeat seatA = saveSeat(room, "A1");
    JSeat seatB = saveSeat(room, "A2");
    JMovie movie = saveMovie("Capacity-Movie-" + UUID.randomUUID());
    JProjection projection = saveProjection(room, movie);
    return new SeededData(room, seatA, seatB, movie, projection);
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
  void manager_can_validate_reservation() {
    String clientToken = registerAndLogin("res-validate-client@example.com", CLIENT);
    String managerToken = registerAndLogin("res-validate-mgr@example.com", MANAGER);
    TestData data = seed();
    ResponseEntity<ReservationResponse> created =
        put("/reservations", reservationBody(data, null), clientToken, ReservationResponse.class);
    ResponseEntity<ReservationResponse> validated =
        put(
            "/reservations/" + created.getBody().id() + "/validate",
            "",
            managerToken,
            ReservationResponse.class);
    assertThat(validated.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(validated.getBody().status()).isEqualTo(ReservationStatus.SUCCESS);
  }

  @Test
  void validate_reservation_insufficient_capacity_returns_409() {
    String clientToken = registerAndLogin("res-cap-client@example.com", CLIENT);
    String managerToken = registerAndLogin("res-cap-mgr@example.com", MANAGER);
    SeededData data = seedWithCapacity(1);

    ResponseEntity<ReservationResponse> first =
        put(
            "/reservations",
            reservationBody(data.projection(), data.seatA()),
            clientToken,
            ReservationResponse.class);
    ResponseEntity<ReservationResponse> validated =
        put(
            "/reservations/" + first.getBody().id() + "/validate",
            "",
            managerToken,
            ReservationResponse.class);
    assertThat(validated.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(validated.getBody().status()).isEqualTo(ReservationStatus.SUCCESS);

    ResponseEntity<ReservationResponse> second =
        put(
            "/reservations",
            reservationBody(data.projection(), data.seatB()),
            clientToken,
            ReservationResponse.class);
    ResponseEntity<Map> tooMany =
        put("/reservations/" + second.getBody().id() + "/validate", "", managerToken, Map.class);
    assertThat(tooMany.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    assertThat(tooMany.getBody()).extracting("error").isEqualTo("CONFLICT");
  }

  @Test
  void cancel_validated_reservation_frees_capacity() {
    String clientToken = registerAndLogin("res-free-client@example.com", CLIENT);
    String managerToken = registerAndLogin("res-free-mgr@example.com", MANAGER);
    SeededData data = seedWithCapacity(1);

    ResponseEntity<ReservationResponse> first =
        put(
            "/reservations",
            reservationBody(data.projection(), data.seatA()),
            clientToken,
            ReservationResponse.class);
    put(
        "/reservations/" + first.getBody().id() + "/validate",
        "",
        managerToken,
        ReservationResponse.class);

    ResponseEntity<ReservationResponse> second =
        put(
            "/reservations",
            reservationBody(data.projection(), data.seatB()),
            clientToken,
            ReservationResponse.class);
    ResponseEntity<Map> blocked =
        put("/reservations/" + second.getBody().id() + "/validate", "", managerToken, Map.class);
    assertThat(blocked.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);

    ResponseEntity<ReservationResponse> canceled =
        put(
            "/reservations/" + first.getBody().id() + "/cancel",
            "",
            managerToken,
            ReservationResponse.class);
    assertThat(canceled.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(canceled.getBody().status()).isEqualTo(ReservationStatus.CANCELED);

    ResponseEntity<ReservationResponse> nowValid =
        put(
            "/reservations/" + second.getBody().id() + "/validate",
            "",
            managerToken,
            ReservationResponse.class);
    assertThat(nowValid.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(nowValid.getBody().status()).isEqualTo(ReservationStatus.SUCCESS);
  }

  @Test
  void validate_already_validated_returns_409() {
    String clientToken = registerAndLogin("res-revalidate-client@example.com", CLIENT);
    String managerToken = registerAndLogin("res-revalidate-mgr@example.com", MANAGER);
    TestData data = seed();
    ResponseEntity<ReservationResponse> created =
        put("/reservations", reservationBody(data, null), clientToken, ReservationResponse.class);
    put(
        "/reservations/" + created.getBody().id() + "/validate",
        "",
        managerToken,
        ReservationResponse.class);
    ResponseEntity<Map> again =
        put("/reservations/" + created.getBody().id() + "/validate", "", managerToken, Map.class);
    assertThat(again.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
  }

  @Test
  void validate_canceled_reservation_returns_409() {
    String clientToken = registerAndLogin("res-valcancel-client@example.com", CLIENT);
    String managerToken = registerAndLogin("res-valcancel-mgr@example.com", MANAGER);
    TestData data = seed();
    ResponseEntity<ReservationResponse> created =
        put("/reservations", reservationBody(data, null), clientToken, ReservationResponse.class);
    put(
        "/reservations/" + created.getBody().id() + "/cancel",
        "",
        managerToken,
        ReservationResponse.class);
    ResponseEntity<Map> validated =
        put("/reservations/" + created.getBody().id() + "/validate", "", managerToken, Map.class);
    assertThat(validated.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
  }

  @Test
  void validate_unknown_reservation_returns_404() {
    String token = registerAndLogin("res-val404@example.com", MANAGER);
    ResponseEntity<Map> response =
        put("/reservations/" + UUID.randomUUID() + "/validate", "", token, Map.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
  }

  @Test
  void client_cannot_validate_via_management_endpoint() {
    String clientToken = registerAndLogin("res-valforbid-client@example.com", CLIENT);
    TestData data = seed();
    ResponseEntity<ReservationResponse> created =
        put("/reservations", reservationBody(data, null), clientToken, ReservationResponse.class);
    ResponseEntity<Map> response =
        put("/reservations/" + created.getBody().id() + "/validate", "", clientToken, Map.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
  }

  @Test
  void employee_cannot_validate_reservation() {
    String clientToken = registerAndLogin("res-valemp-client@example.com", CLIENT);
    String employeeToken = registerAndLogin("res-valemp@example.com", EMPLOYEE);
    TestData data = seed();
    ResponseEntity<ReservationResponse> created =
        put("/reservations", reservationBody(data, null), clientToken, ReservationResponse.class);
    ResponseEntity<Map> response =
        put("/reservations/" + created.getBody().id() + "/validate", "", employeeToken, Map.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
  }

  @Test
  void manager_can_cancel_reservation() {
    String clientToken = registerAndLogin("res-cancel-client@example.com", CLIENT);
    String managerToken = registerAndLogin("res-cancel-mgr@example.com", MANAGER);
    TestData data = seed();
    ResponseEntity<ReservationResponse> created =
        put("/reservations", reservationBody(data, null), clientToken, ReservationResponse.class);
    ResponseEntity<ReservationResponse> canceled =
        put(
            "/reservations/" + created.getBody().id() + "/cancel",
            "",
            managerToken,
            ReservationResponse.class);
    assertThat(canceled.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(canceled.getBody().status()).isEqualTo(ReservationStatus.CANCELED);
  }

  @Test
  void cancel_already_canceled_returns_409() {
    String clientToken = registerAndLogin("res-recancel-client@example.com", CLIENT);
    String managerToken = registerAndLogin("res-recancel-mgr@example.com", MANAGER);
    TestData data = seed();
    ResponseEntity<ReservationResponse> created =
        put("/reservations", reservationBody(data, null), clientToken, ReservationResponse.class);
    put(
        "/reservations/" + created.getBody().id() + "/cancel",
        "",
        managerToken,
        ReservationResponse.class);
    ResponseEntity<Map> again =
        put("/reservations/" + created.getBody().id() + "/cancel", "", managerToken, Map.class);
    assertThat(again.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
  }

  @Test
  void cancel_unknown_reservation_returns_404() {
    String token = registerAndLogin("res-cancel404@example.com", MANAGER);
    ResponseEntity<Map> response =
        put("/reservations/" + UUID.randomUUID() + "/cancel", "", token, Map.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
  }

  @Test
  void client_cannot_cancel_via_management_endpoint() {
    String clientToken = registerAndLogin("res-cancelforbid-client@example.com", CLIENT);
    TestData data = seed();
    ResponseEntity<ReservationResponse> created =
        put("/reservations", reservationBody(data, null), clientToken, ReservationResponse.class);
    ResponseEntity<Map> response =
        put("/reservations/" + created.getBody().id() + "/cancel", "", clientToken, Map.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
  }

  @Test
  void employee_cannot_cancel_reservation() {
    String clientToken = registerAndLogin("res-cancelemp-client@example.com", CLIENT);
    String employeeToken = registerAndLogin("res-cancelemp@example.com", EMPLOYEE);
    TestData data = seed();
    ResponseEntity<ReservationResponse> created =
        put("/reservations", reservationBody(data, null), clientToken, ReservationResponse.class);
    ResponseEntity<Map> response =
        put("/reservations/" + created.getBody().id() + "/cancel", "", employeeToken, Map.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
  }

  @Test
  void employee_cannot_validate_when_room_full_via_upsert() {
    String employeeToken = registerAndLogin("res-upsert-cap-emp@example.com", EMPLOYEE);
    SeededData data = seedWithCapacity(1);
    ResponseEntity<ReservationResponse> first =
        put(
            "/reservations",
            reservationBody(data.projection(), data.seatA(), "SUCCESS"),
            employeeToken,
            ReservationResponse.class);
    assertThat(first.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(first.getBody().status()).isEqualTo(ReservationStatus.SUCCESS);

    ResponseEntity<Map> second =
        put(
            "/reservations",
            reservationBody(data.projection(), data.seatB(), "SUCCESS"),
            employeeToken,
            Map.class);
    assertThat(second.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    assertThat(second.getBody()).extracting("error").isEqualTo("CONFLICT");
  }

  @Test
  void validate_reservation_with_already_validated_seat_returns_409() {
    String clientToken = registerAndLogin("res-seatconf-client@example.com", CLIENT);
    String managerToken = registerAndLogin("res-seatconf-mgr@example.com", MANAGER);
    SeededData data = seedWithCapacity(2);
    ResponseEntity<ReservationResponse> first =
        put(
            "/reservations",
            reservationBody(data.projection(), data.seatA()),
            clientToken,
            ReservationResponse.class);
    ResponseEntity<ReservationResponse> second =
        put(
            "/reservations",
            reservationBody(data.projection(), data.seatA()),
            clientToken,
            ReservationResponse.class);

    ResponseEntity<ReservationResponse> validated =
        put(
            "/reservations/" + first.getBody().id() + "/validate",
            "",
            managerToken,
            ReservationResponse.class);
    assertThat(validated.getStatusCode()).isEqualTo(HttpStatus.OK);

    ResponseEntity<Map> conflict =
        put("/reservations/" + second.getBody().id() + "/validate", "", managerToken, Map.class);
    assertThat(conflict.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    assertThat(conflict.getBody()).extracting("error").isEqualTo("CONFLICT");
  }

  @Test
  void upsert_success_with_already_validated_seat_returns_409() {
    String clientToken = registerAndLogin("res-upsert-seat-client@example.com", CLIENT);
    String managerToken = registerAndLogin("res-upsert-seat-mgr@example.com", MANAGER);
    String employeeToken = registerAndLogin("res-upsert-seat-emp@example.com", EMPLOYEE);
    SeededData data = seedWithCapacity(2);
    ResponseEntity<ReservationResponse> created =
        put(
            "/reservations",
            reservationBody(data.projection(), data.seatA()),
            clientToken,
            ReservationResponse.class);
    ResponseEntity<ReservationResponse> validated =
        put(
            "/reservations/" + created.getBody().id() + "/validate",
            "",
            managerToken,
            ReservationResponse.class);
    assertThat(validated.getStatusCode()).isEqualTo(HttpStatus.OK);

    ResponseEntity<Map> conflict =
        put(
            "/reservations",
            reservationBody(data.projection(), data.seatA(), "SUCCESS"),
            employeeToken,
            Map.class);
    assertThat(conflict.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
  }

  @Test
  void upsert_success_is_idempotent_for_same_reservation() {
    String clientToken = registerAndLogin("res-idempotent-client@example.com", CLIENT);
    String managerToken = registerAndLogin("res-idempotent-mgr@example.com", MANAGER);
    TestData data = seed();
    ResponseEntity<ReservationResponse> created =
        put("/reservations", reservationBody(data, null), clientToken, ReservationResponse.class);
    UUID reservationId = created.getBody().id();

    ResponseEntity<ReservationResponse> firstValidate =
        put(
            "/reservations",
            updateBody(data, reservationId, "SUCCESS"),
            managerToken,
            ReservationResponse.class);
    assertThat(firstValidate.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(firstValidate.getBody().status()).isEqualTo(ReservationStatus.SUCCESS);

    ResponseEntity<ReservationResponse> secondValidate =
        put(
            "/reservations",
            updateBody(data, reservationId, "SUCCESS"),
            managerToken,
            ReservationResponse.class);
    assertThat(secondValidate.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(secondValidate.getBody().status()).isEqualTo(ReservationStatus.SUCCESS);
  }

  @Test
  void shouldReturnNullWhenInputIsNull() {
    JReservation jReservation = new JReservation();
    assertNull(reservationMapper.toResponse(null));
    assertNull(reservationMapper.toJReservation(null, jReservation, UUID.randomUUID()));
  }
}
