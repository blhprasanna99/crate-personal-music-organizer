package com.crate.backend.web.dto;

import java.util.List;

public record ImportSummary(
        int scanned,
        int imported,
        int skipped,
        int failed,
        List<ImportFailure> errors
) {
    public record ImportFailure(String path, String reason) {}
}
