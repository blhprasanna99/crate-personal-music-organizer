package com.crate.backend.web.dto;

import java.util.List;

public record SmartCrateUpdateRequest(
        String name,
        String description,
        List<Filter> criteria
) {}
