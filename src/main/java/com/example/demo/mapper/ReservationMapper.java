package com.example.demo.mapper;

import com.example.demo.dto.reservation.ReservationResponse;
import com.example.demo.dto.reservation.ReservationUpsertRequest;
import com.example.demo.exception.NotFoundException;
import com.example.demo.repository.ProjectionRepository;
import com.example.demo.repository.SeatRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.repository.model.JProjection;
import com.example.demo.repository.model.JReservation;
import com.example.demo.repository.model.JSeat;
import com.example.demo.repository.model.JUser;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class ReservationMapper {

  private ProjectionRepository projectionRepository;
  private UserRepository userRepository;
  private SeatRepository seatRepository;

  public ReservationResponse toResponse(JReservation reservation) {
    if (reservation == null) {
      return null;
    }
    return new ReservationResponse(
        reservation.getId(),
        reservation.getCreatedAt(),
        reservation.getStatus(),
        reservation.getProjection().getId(),
        reservation.getUser().getId(),
        reservation.getSeats().stream().map(JSeat::getId).sorted().toList());
  }

  public JReservation toJReservation(
      ReservationUpsertRequest req, JReservation reservation, UUID userId) {
        if(req == null){
          return null;
        }
    JProjection projection =
        projectionRepository
            .findById(req.projectionId())
            .orElseThrow(
                () -> new NotFoundException("Projection not found with id: " + req.projectionId()));
    JUser user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new NotFoundException("User not found with id: " + userId));
    Set<JSeat> seats =
        req.seatIds().stream()
            .map(
                seatId ->
                    seatRepository
                        .findById(seatId)
                        .orElseThrow(
                            () -> new NotFoundException("Seat not found with id: " + seatId)))
            .collect(Collectors.toSet());
    reservation.setProjection(projection);
    if (reservation.getUser() == null) {
      reservation.setUser(user);
    }
    reservation.setSeats(seats);
    return reservation;
  }
}
