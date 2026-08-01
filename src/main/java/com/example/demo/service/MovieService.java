package com.example.demo.service;

import com.example.demo.constant.UserRole;
import com.example.demo.dto.movie.MovieResponse;
import com.example.demo.dto.movie.MovieUpsertRequest;
import com.example.demo.exception.ForbiddenException;
import com.example.demo.exception.NotFoundException;
import com.example.demo.mapper.MovieMapper;
import com.example.demo.model.User;
import com.example.demo.repository.MovieRepository;
import com.example.demo.repository.model.JMovie;
import jakarta.transaction.Transactional;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class MovieService {

  private MovieRepository movieRepository;
  private MovieMapper movieMapper;

  public List<MovieResponse> getAllMovies() {
    return movieRepository.findAll().stream().map(movieMapper::toResponse).toList();
  }

  @Transactional
  public MovieResponse upsertMovie(MovieUpsertRequest req, User currentUser) {
    if (currentUser.getRole() != UserRole.MANAGER) {
      throw new ForbiddenException("Access denied: only MANAGER can create or update movies");
    }
    JMovie movie;
    if (req.id() != null) {
      movie =
          movieRepository
              .findById(req.id())
              .orElseThrow(() -> new NotFoundException("Movie not found with id: " + req.id()));
    } else {
      movie = new JMovie();
    }
    movieMapper.toJMovie(req, movie);
    return movieMapper.toResponse(movieRepository.save(movie));
  }
}
