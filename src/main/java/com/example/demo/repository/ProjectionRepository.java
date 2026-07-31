package com.example.demo.repository;

import com.example.demo.repository.model.JProjection;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProjectionRepository extends JpaRepository<JProjection, UUID> {

  List<JProjection> findByMovieId(UUID movieId);

  List<JProjection> findByRoomId(UUID roomId);
}
