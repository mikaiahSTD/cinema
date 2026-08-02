package com.example.demo.dto.movie;

import com.example.demo.constant.Genre;
import java.util.List;
import java.util.UUID;

public record MovieResponse(
    UUID id, String title, List<Genre> genres, String description, String duration) {}
