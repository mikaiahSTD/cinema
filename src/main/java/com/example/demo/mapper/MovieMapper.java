package com.example.demo.mapper;

import com.example.demo.dto.movie.MovieResponse;
import com.example.demo.dto.movie.MovieUpsertRequest;
import com.example.demo.exception.BadRequestException;
import com.example.demo.repository.model.JMovie;
import java.time.Duration;
import java.time.format.DateTimeParseException;
import java.util.HashSet;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class MovieMapper {

  public MovieResponse toResponse(JMovie movie) {
    if (movie == null) {
      return null;
    }
    return new MovieResponse(
        movie.getId(),
        movie.getTitle(),
        movie.getGenres().stream().sorted().collect(Collectors.toList()),
        movie.getDescription(),
        movie.getDuration() == null ? null : movie.getDuration().toString());
  }

  public JMovie toJMovie(MovieUpsertRequest req, JMovie movie) {
    if (req == null) {
      return null;
    }
    movie.setTitle(req.title());
    movie.setGenres(new HashSet<>(req.genres()));
    movie.setDescription(req.description());
    movie.setDuration(parseDuration(req.duration()));
    return movie;
  }

  private Duration parseDuration(String value) {
    try {
      return Duration.parse(value);
    } catch (DateTimeParseException e) {
      throw new BadRequestException("Invalid duration format: " + value);
    }
  }
}
