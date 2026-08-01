package com.example.demo.service;

import com.example.demo.constant.UserRole;
import com.example.demo.dto.projection.ProjectionResponse;
import com.example.demo.dto.projection.ProjectionUpsertRequest;
import com.example.demo.exception.ForbiddenException;
import com.example.demo.exception.NotFoundException;
import com.example.demo.mapper.ProjectionMapper;
import com.example.demo.model.User;
import com.example.demo.repository.ProjectionRepository;
import com.example.demo.repository.model.JProjection;
import jakarta.transaction.Transactional;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class ProjectionService {

  private ProjectionRepository projectionRepository;
  private ProjectionMapper projectionMapper;

  public List<ProjectionResponse> getAllProjections() {
    return projectionRepository.findAll().stream().map(projectionMapper::toResponse).toList();
  }

  @Transactional
  public ProjectionResponse upsertProjection(ProjectionUpsertRequest req, User currentUser) {
    if (currentUser.getRole() != UserRole.MANAGER) {
      throw new ForbiddenException("Access denied: only MANAGER can create or update projections");
    }
    JProjection projection;
    if (req.id() != null) {
      projection =
          projectionRepository
              .findById(req.id())
              .orElseThrow(
                  () ->
                      new NotFoundException("Projection not found with id: " + req.id()));
    } else {
      projection = new JProjection();
    }
    projectionMapper.toJProjection(req, projection);
    return projectionMapper.toResponse(projectionRepository.save(projection));
  }
}
