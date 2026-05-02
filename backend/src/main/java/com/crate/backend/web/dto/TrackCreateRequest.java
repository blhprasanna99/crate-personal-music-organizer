package com.crate.backend.web.dto;

import jakarta.validation.constraints.NotBlank;

public record TrackCreateRequest(
        @NotBlank String filePath,
        String title,
        String artist,
        String album,
        Integer durationMs
) {}
