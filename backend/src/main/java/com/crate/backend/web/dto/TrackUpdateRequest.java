package com.crate.backend.web.dto;

public record TrackUpdateRequest(
        String title,
        String artist,
        String album,
        Integer durationMs
) {}
