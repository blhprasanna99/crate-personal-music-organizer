package com.crate.backend.web.dto;

import com.crate.backend.entity.Tag;

import java.time.OffsetDateTime;

public record TagResponse(
        Long id,
        String name,
        OffsetDateTime createdAt
) {
    public static TagResponse from(Tag t) {
        return new TagResponse(t.getId(), t.getName(), t.getCreatedAt());
    }
}
