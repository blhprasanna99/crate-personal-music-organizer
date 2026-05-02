package com.crate.backend.web.dto;

import jakarta.validation.constraints.NotNull;

import java.util.List;

public record SearchRequest(
        @NotNull List<Filter> criteria
) {}
