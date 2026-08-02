package com.example.demo.endpoint.rest.controller;

import com.example.demo.dto.movie.MovieResponse;
import com.example.demo.dto.movie.MovieUpsertRequest;
import com.example.demo.pageable.Page;
import com.example.demo.service.MovieService;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@RequestMapping("/movies")
public class MovieController {

  private static final int MAX_PAGE_SIZE = 100;

  private MovieService movieService;

  @GetMapping
  public ResponseEntity<Page<MovieResponse>> getMovies(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int pageSize) {
    Pageable pageable = PageRequest.of(page, Math.min(pageSize, MAX_PAGE_SIZE));
    return ResponseEntity.ok().body(movieService.getAllMovies(pageable));
  }

  @GetMapping("/{id}")
  public ResponseEntity<MovieResponse> getMovieById(@PathVariable UUID id) {
    return ResponseEntity.ok().body(movieService.getMovieById(id));
  }

  @PutMapping
  public ResponseEntity<MovieResponse> upsertMovie(@RequestBody @Valid MovieUpsertRequest req) {
    return ResponseEntity.ok().body(movieService.upsertMovie(req));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteMovie(@PathVariable UUID id) {
    movieService.deleteMovie(id);
    return ResponseEntity.ok().build();
  }
}
