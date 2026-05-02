package com.crate.backend.web.dto;

import jakarta.validation.constraints.NotBlank;

public record TagCreateRequest(
        @NotBlank String name
) {}
