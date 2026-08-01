package com.example.demo.endpoint.rest.controller;

import com.example.demo.dto.projection.ProjectionResponse;
import com.example.demo.dto.projection.ProjectionUpsertRequest;
import com.example.demo.service.ProjectionService;
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
@RequestMapping("/projections")
public class ProjectionController {

  private ProjectionService projectionService;

  @GetMapping
  public ResponseEntity<List<ProjectionResponse>> getProjections() {
    return ResponseEntity.ok().body(projectionService.getAllProjections());
  }

  @PutMapping
  public ResponseEntity<ProjectionResponse> upsertProjection(
      @RequestBody @Valid ProjectionUpsertRequest req) {
    return ResponseEntity.ok().body(projectionService.upsertProjection(req));
  }
}
