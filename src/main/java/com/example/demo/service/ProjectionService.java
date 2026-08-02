package com.example.demo.service;

import com.example.demo.dto.projection.ProjectionResponse;
import com.example.demo.dto.projection.ProjectionUpsertRequest;
import com.example.demo.exception.ConflictException;
import com.example.demo.exception.NotFoundException;
import com.example.demo.mapper.ProjectionMapper;
import com.example.demo.pageable.Page;
import com.example.demo.repository.ProjectionRepository;
import com.example.demo.repository.model.JProjection;
import jakarta.transaction.Transactional;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class ProjectionService {

  private ProjectionRepository projectionRepository;
  private ProjectionMapper projectionMapper;

  public Page<ProjectionResponse> getAllProjections(Pageable pageable) {
    return Page.from(projectionRepository.findAll(pageable).map(projectionMapper::toResponse));
  }

  public ProjectionResponse getProjectionById(UUID id) {
    JProjection projection =
        projectionRepository
            .findById(id)
            .orElseThrow(() -> new NotFoundException("Projection not found with id: " + id));
    return projectionMapper.toResponse(projection);
  }

  @Transactional
  public void deleteProjection(UUID id) {
    JProjection projection =
        projectionRepository
            .findById(id)
            .orElseThrow(() -> new NotFoundException("Projection not found with id: " + id));
    if (!projection.getReservations().isEmpty()) {
      throw new ConflictException("Cannot delete projection with existing reservations");
    }
    projectionRepository.delete(projection);
  }

  @Transactional
  public ProjectionResponse upsertProjection(ProjectionUpsertRequest req) {
    JProjection projection;
    if (req.id() != null) {
      projection =
          projectionRepository
              .findById(req.id())
              .orElseThrow(
                  () -> new NotFoundException("Projection not found with id: " + req.id()));
    } else {
      projection = new JProjection();
    }
    projectionMapper.toJProjection(req, projection);
    return projectionMapper.toResponse(projectionRepository.save(projection));
  }
}
