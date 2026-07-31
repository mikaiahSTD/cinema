package com.example.demo.repositories;

import com.example.demo.models.Projection;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProjectionRepository extends JpaRepository<Projection, UUID> {

  List<Projection> findByMovieId(UUID movieId);

  List<Projection> findByRoomId(UUID roomId);
}
