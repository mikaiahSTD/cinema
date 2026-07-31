package com.example.demo.repository;

import com.example.demo.repository.model.JRoom;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoomRepository extends JpaRepository<JRoom, UUID> {

  Optional<JRoom> findByNumber(String number);
}
