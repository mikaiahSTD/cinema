package com.example.demo.repositories;

import com.example.demo.enums.Genre;
import com.example.demo.models.Movie;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MovieRepository extends JpaRepository<Movie, UUID> {

  List<Movie> findByGenresContaining(Genre genre);

  List<Movie> findByTitleContainingIgnoreCase(String title);
}
