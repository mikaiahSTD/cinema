package com.example.demo.repositories;

import com.example.demo.models.Room;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoomRepository extends JpaRepository<Room, UUID> {

  Optional<Room> findByNumber(String number);
}
