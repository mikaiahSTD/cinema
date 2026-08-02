package com.example.demo.endpoint.rest.controller;

import com.example.demo.dto.reservation.ReservationResponse;
import com.example.demo.dto.reservation.ReservationUpsertRequest;
import com.example.demo.pageable.Page;
import com.example.demo.security.UserPrincipal;
import com.example.demo.service.ReservationService;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@RequestMapping("/reservations")
public class ReservationController {

  private static final int MAX_PAGE_SIZE = 100;

  private ReservationService reservationService;

  @GetMapping
  public ResponseEntity<Page<ReservationResponse>> getReservations(
      @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int pageSize) {
    Pageable pageable = PageRequest.of(page, Math.min(pageSize, MAX_PAGE_SIZE));
    return ResponseEntity.ok().body(reservationService.getAllReservations(pageable));
  }

  @GetMapping("/{id}")
  public ResponseEntity<ReservationResponse> getReservationById(
      @PathVariable UUID id, @AuthenticationPrincipal UserPrincipal principal) {
    return ResponseEntity.ok().body(reservationService.getReservationById(id, principal.getUser()));
  }

  @PutMapping
  public ResponseEntity<ReservationResponse> upsertReservation(
      @RequestBody @Valid ReservationUpsertRequest req,
      @AuthenticationPrincipal UserPrincipal principal) {
    return ResponseEntity.ok().body(reservationService.upsertReservation(req, principal.getUser()));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteReservation(@PathVariable UUID id) {
    reservationService.deleteReservation(id);
    return ResponseEntity.ok().build();
  }
}
