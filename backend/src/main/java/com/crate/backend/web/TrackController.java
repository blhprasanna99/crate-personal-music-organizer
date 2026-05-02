package com.crate.backend.web;

import com.crate.backend.service.FolderImportService;
import com.crate.backend.service.TrackService;
import com.crate.backend.web.dto.ImportRequest;
import com.crate.backend.web.dto.ImportSummary;
import com.crate.backend.web.dto.TrackCreateRequest;
import com.crate.backend.web.dto.TrackResponse;
import com.crate.backend.web.dto.TrackUpdateRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/tracks")
@RequiredArgsConstructor
public class TrackController {

    private final TrackService tracks;
    private final FolderImportService folderImport;

    @GetMapping
    public List<TrackResponse> list() {
        return tracks.list().stream().map(TrackResponse::from).toList();
    }

    @GetMapping("/{id}")
    public TrackResponse get(@PathVariable Long id) {
        return TrackResponse.from(tracks.get(id));
    }

    @PostMapping
    public ResponseEntity<TrackResponse> create(@Valid @RequestBody TrackCreateRequest req) {
        var saved = tracks.create(req);
        return ResponseEntity.created(URI.create("/api/tracks/" + saved.getId()))
                .body(TrackResponse.from(saved));
    }

    @PatchMapping("/{id}")
    public TrackResponse update(@PathVariable Long id, @RequestBody TrackUpdateRequest req) {
        return TrackResponse.from(tracks.update(id, req));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        tracks.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/import")
    public ImportSummary importFolder(@Valid @RequestBody ImportRequest req) throws IOException {
        return folderImport.importFolder(req.folderPath());
    }
}
