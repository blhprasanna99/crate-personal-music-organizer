package com.crate.backend.web.dto;

public record CrateUpdateRequest(
        String name,
        String description
) {}
