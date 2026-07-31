package com.example.demo.repositories;

import com.example.demo.models.Seat;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SeatRepository extends JpaRepository<Seat, UUID> {

  List<Seat> findByRoomId(UUID roomId);
}
