package com.crate.backend.web.dto;

import jakarta.validation.constraints.NotBlank;

public record CrateCreateRequest(
        @NotBlank String name,
        String description
) {}
