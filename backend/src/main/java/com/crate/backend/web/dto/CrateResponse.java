package com.crate.backend.web.dto;

import com.crate.backend.entity.Crate;

import java.time.OffsetDateTime;

public record CrateResponse(
        Long id,
        String name,
        String description,
        OffsetDateTime createdAt
) {
    public static CrateResponse from(Crate c) {
        return new CrateResponse(c.getId(), c.getName(), c.getDescription(), c.getCreatedAt());
    }
}
