package com.example.demo.repository;

import com.example.demo.constant.Genre;
import com.example.demo.repository.model.JMovie;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MovieRepository extends JpaRepository<JMovie, UUID> {

  List<JMovie> findByGenresContaining(Genre genre);

  List<JMovie> findByTitleContainingIgnoreCase(String title);
}
