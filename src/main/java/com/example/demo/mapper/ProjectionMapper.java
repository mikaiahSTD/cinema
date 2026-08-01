package com.example.demo.mapper;

import com.example.demo.dto.projection.ProjectionResponse;
import com.example.demo.dto.projection.ProjectionUpsertRequest;
import com.example.demo.exception.NotFoundException;
import com.example.demo.repository.MovieRepository;
import com.example.demo.repository.RoomRepository;
import com.example.demo.repository.model.JMovie;
import com.example.demo.repository.model.JProjection;
import com.example.demo.repository.model.JRoom;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class ProjectionMapper {

  private RoomRepository roomRepository;
  private MovieRepository movieRepository;

  public ProjectionResponse toResponse(JProjection projection) {
    if (projection == null) {
      return null;
    }
    return new ProjectionResponse(
        projection.getId(),
        projection.getDatetime(),
        projection.getSeatPrice(),
        projection.getRoom().getId(),
        projection.getMovie().getId());
  }

  public JProjection toJProjection(ProjectionUpsertRequest req, JProjection projection) {
    if(req == null){
      return null;
    }
    JRoom room =
        roomRepository
            .findById(req.roomId())
            .orElseThrow(() -> new NotFoundException("Room not found with id: " + req.roomId()));
    JMovie movie =
        movieRepository
            .findById(req.movieId())
            .orElseThrow(() -> new NotFoundException("Movie not found with id: " + req.movieId()));
    projection.setDatetime(req.datetime());
    projection.setSeatPrice(req.seatPrice());
    projection.setRoom(room);
    projection.setMovie(movie);
    return projection;
  }
}
