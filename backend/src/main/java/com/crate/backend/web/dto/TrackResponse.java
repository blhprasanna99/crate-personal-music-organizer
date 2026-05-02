package com.crate.backend.web.dto;

import com.crate.backend.entity.Track;

import java.time.OffsetDateTime;

public record TrackResponse(
        Long id,
        String filePath,
        String title,
        String artist,
        String album,
        Integer durationMs,
        OffsetDateTime createdAt
) {
    public static TrackResponse from(Track t) {
        return new TrackResponse(
                t.getId(),
                t.getFilePath(),
                t.getTitle(),
                t.getArtist(),
                t.getAlbum(),
                t.getDurationMs(),
                t.getCreatedAt()
        );
    }
}
