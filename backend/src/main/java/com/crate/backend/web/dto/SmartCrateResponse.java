package com.crate.backend.web.dto;

import com.crate.backend.entity.SmartCrate;

import java.time.OffsetDateTime;
import java.util.List;

public record SmartCrateResponse(
        Long id,
        String name,
        String description,
        List<Filter> criteria,
        OffsetDateTime createdAt
) {
    public static SmartCrateResponse from(SmartCrate s) {
        return new SmartCrateResponse(
                s.getId(), s.getName(), s.getDescription(), s.getCriteria(), s.getCreatedAt()
        );
    }
}
