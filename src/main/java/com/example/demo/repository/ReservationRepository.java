package com.example.demo.repository;

import com.example.demo.constant.ReservationStatus;
import com.example.demo.repository.model.JReservation;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReservationRepository extends JpaRepository<JReservation, UUID> {

  List<JReservation> findByUserId(UUID userId);

  List<JReservation> findByProjectionId(UUID projectionId);

  List<JReservation> findByStatus(ReservationStatus status);
}
