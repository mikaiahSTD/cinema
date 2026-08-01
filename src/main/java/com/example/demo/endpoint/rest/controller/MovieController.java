package com.example.demo.endpoint.rest.controller;

import com.example.demo.dto.movie.MovieResponse;
import com.example.demo.dto.movie.MovieUpsertRequest;
import com.example.demo.service.MovieService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@RequestMapping("/movies")
public class MovieController {

  private MovieService movieService;

  @GetMapping
  public ResponseEntity<List<MovieResponse>> getMovies() {
    return ResponseEntity.ok().body(movieService.getAllMovies());
  }

  @PutMapping
  public ResponseEntity<MovieResponse> upsertMovie(@RequestBody @Valid MovieUpsertRequest req) {
    return ResponseEntity.ok().body(movieService.upsertMovie(req));
  }
}
