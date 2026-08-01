package com.example.demo.dto.movie;

import com.example.demo.constant.Genre;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import java.util.UUID;

public record MovieUpsertRequest(
    UUID id,
    @NotBlank(message = "Title is required") String title,
    @NotEmpty(message = "Genres must not be empty") List<Genre> genres,
    String description,
    @NotBlank(message = "Duration is required") String duration) {}
