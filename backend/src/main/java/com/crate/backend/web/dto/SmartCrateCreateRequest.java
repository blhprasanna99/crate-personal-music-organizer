package com.crate.backend.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record SmartCrateCreateRequest(
        @NotBlank String name,
        String description,
        @NotNull List<Filter> criteria
) {}
