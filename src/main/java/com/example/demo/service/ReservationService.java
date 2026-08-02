package com.example.demo.service;

import com.example.demo.constant.ReservationStatus;
import com.example.demo.constant.UserRole;
import com.example.demo.dto.reservation.ReservationResponse;
import com.example.demo.dto.reservation.ReservationUpsertRequest;
import com.example.demo.exception.ForbiddenException;
import com.example.demo.exception.NotFoundException;
import com.example.demo.mapper.ReservationMapper;
import com.example.demo.model.User;
import com.example.demo.pageable.Page;
import com.example.demo.repository.ReservationRepository;
import com.example.demo.repository.model.JReservation;
import jakarta.transaction.Transactional;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class ReservationService {

  private ReservationRepository reservationRepository;
  private ReservationMapper reservationMapper;

  @Transactional
  public Page<ReservationResponse> getAllReservations(Pageable pageable) {
    return Page.from(reservationRepository.findAll(pageable).map(reservationMapper::toResponse));
  }

  @Transactional
  public ReservationResponse getReservationById(UUID id, User currentUser) {
    JReservation reservation =
        reservationRepository
            .findById(id)
            .orElseThrow(() -> new NotFoundException("Reservation not found with id: " + id));
    if (currentUser.getRole() == UserRole.CLIENT
        && !reservation.getUser().getId().equals(currentUser.getId())) {
      throw new ForbiddenException(
          "Access denied: reservation does not belong to the current user");
    }
    return reservationMapper.toResponse(reservation);
  }

  @Transactional
  public ReservationResponse upsertReservation(ReservationUpsertRequest req, User currentUser) {
    boolean isStaff =
        currentUser.getRole() == UserRole.EMPLOYEE || currentUser.getRole() == UserRole.MANAGER;
    ReservationStatus requestedStatus =
        req.status() == null ? ReservationStatus.PENDING : req.status();

    JReservation reservation;
    if (req.id() != null) {
      reservation =
          reservationRepository
              .findById(req.id())
              .orElseThrow(
                  () -> new NotFoundException("Reservation not found with id: " + req.id()));
      if (!isStaff && !reservation.getUser().getId().equals(currentUser.getId())) {
        throw new ForbiddenException(
            "Access denied: cannot modify a reservation that is not your own");
      }
    } else {
      reservation = new JReservation();
      reservation.setCreatedAt(Instant.now());
    }

    if (!isStaff && requestedStatus != ReservationStatus.PENDING) {
      throw new ForbiddenException(
          "Only EMPLOYEE or MANAGER can set reservation status to " + requestedStatus);
    }

    reservationMapper.toJReservation(req, reservation, currentUser.getId());
    reservation.setStatus(requestedStatus);

    return reservationMapper.toResponse(reservationRepository.save(reservation));
  }

  @Transactional
  public void deleteReservation(UUID id) {
    JReservation reservation =
        reservationRepository
            .findById(id)
            .orElseThrow(() -> new NotFoundException("Reservation not found with id: " + id));
    reservationRepository.delete(reservation);
  }
}
