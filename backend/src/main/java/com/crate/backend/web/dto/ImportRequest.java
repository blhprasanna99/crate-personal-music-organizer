package com.crate.backend.web.dto;

import jakarta.validation.constraints.NotBlank;

public record ImportRequest(
        @NotBlank String folderPath
) {}
