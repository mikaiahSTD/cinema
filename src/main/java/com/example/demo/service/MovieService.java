package com.example.demo.service;

import com.example.demo.dto.movie.MovieResponse;
import com.example.demo.dto.movie.MovieUpsertRequest;
import com.example.demo.exception.ConflictException;
import com.example.demo.exception.NotFoundException;
import com.example.demo.mapper.MovieMapper;
import com.example.demo.pageable.Page;
import com.example.demo.repository.MovieRepository;
import com.example.demo.repository.model.JMovie;
import jakarta.transaction.Transactional;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class MovieService {

  private MovieRepository movieRepository;
  private MovieMapper movieMapper;

  public Page<MovieResponse> getAllMovies(Pageable pageable) {
    return Page.from(movieRepository.findAll(pageable).map(movieMapper::toResponse));
  }

  public MovieResponse getMovieById(UUID id) {
    JMovie movie =
        movieRepository
            .findById(id)
            .orElseThrow(() -> new NotFoundException("Movie not found with id: " + id));
    return movieMapper.toResponse(movie);
  }

  @Transactional
  public void deleteMovie(UUID id) {
    JMovie movie =
        movieRepository
            .findById(id)
            .orElseThrow(() -> new NotFoundException("Movie not found with id: " + id));
    if (!movie.getProjections().isEmpty()) {
      throw new ConflictException("Cannot delete movie with existing projections");
    }
    movieRepository.delete(movie);
  }

  @Transactional
  public MovieResponse upsertMovie(MovieUpsertRequest req) {
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
