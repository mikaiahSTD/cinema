package com.example.demo.pageable;

import java.util.List;

public record Page<T>(List<T> content, int page, int pageSize, long totalElements, int totalPages) {
  public static <T> Page<T> from(org.springframework.data.domain.Page<T> source) {
    return new Page<>(
        source.getContent(),
        source.getNumber(),
        source.getSize(),
        source.getTotalElements(),
        source.getTotalPages());
  }
}
