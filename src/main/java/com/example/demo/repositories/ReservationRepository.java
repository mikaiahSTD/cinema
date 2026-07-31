package com.example.demo.repositories;

import com.example.demo.enums.ReservationStatus;
import com.example.demo.models.Reservation;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, UUID> {

  List<Reservation> findByUserId(UUID userId);

  List<Reservation> findByProjectionId(UUID projectionId);

  List<Reservation> findByStatus(ReservationStatus status);
}
