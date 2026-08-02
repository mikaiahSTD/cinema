package com.example.demo.endpoint.rest.controller;

import com.example.demo.dto.projection.ProjectionResponse;
import com.example.demo.dto.projection.ProjectionUpsertRequest;
import com.example.demo.pageable.Page;
import com.example.demo.service.ProjectionService;
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
@RequestMapping("/projections")
public class ProjectionController {

  private static final int MAX_PAGE_SIZE = 100;

  private ProjectionService projectionService;

  @GetMapping
  public ResponseEntity<Page<ProjectionResponse>> getProjections(
      @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int pageSize) {
    Pageable pageable = PageRequest.of(page, Math.min(pageSize, MAX_PAGE_SIZE));
    return ResponseEntity.ok().body(projectionService.getAllProjections(pageable));
  }

  @GetMapping("/{id}")
  public ResponseEntity<ProjectionResponse> getProjectionById(@PathVariable UUID id) {
    return ResponseEntity.ok().body(projectionService.getProjectionById(id));
  }

  @PutMapping
  public ResponseEntity<ProjectionResponse> upsertProjection(
      @RequestBody @Valid ProjectionUpsertRequest req) {
    return ResponseEntity.ok().body(projectionService.upsertProjection(req));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteProjection(@PathVariable UUID id) {
    projectionService.deleteProjection(id);
    return ResponseEntity.ok().build();
  }
}
