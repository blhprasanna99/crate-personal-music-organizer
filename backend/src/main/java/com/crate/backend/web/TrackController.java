package com.crate.backend.web;

import com.crate.backend.service.FilterEngine;
import com.crate.backend.service.FolderImportService;
import com.crate.backend.service.TagService;
import com.crate.backend.service.TrackService;
import com.crate.backend.web.dto.ImportRequest;
import com.crate.backend.web.dto.ImportSummary;
import com.crate.backend.web.dto.SearchRequest;
import com.crate.backend.web.dto.TagResponse;
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
    private final TagService tagService;
    private final FilterEngine filterEngine;

    @GetMapping
    public List<TrackResponse> list(
            @RequestParam(defaultValue = "false") boolean trashed
    ) {
        var list = trashed ? tracks.listTrashed() : tracks.list();
        return list.stream().map(TrackResponse::from).toList();
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
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            @RequestParam(defaultValue = "trash") String mode
    ) {
        switch (mode) {
            case "trash" -> tracks.delete(id);
            case "purge" -> tracks.purge(id);
            default -> throw new IllegalArgumentException("mode must be 'trash' or 'purge'");
        }
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/restore")
    public ResponseEntity<Void> restore(@PathVariable Long id) {
        tracks.restore(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/import")
    public ImportSummary importFolder(@Valid @RequestBody ImportRequest req) throws IOException {
        return folderImport.importFolder(req.folderPath());
    }

    @PostMapping("/search")
    public List<TrackResponse> search(@Valid @RequestBody SearchRequest req) {
        return filterEngine.resolve(req.criteria()).stream().map(TrackResponse::from).toList();
    }

    @GetMapping("/{trackId}/tags")
    public List<TagResponse> tagsOf(@PathVariable Long trackId) {
        return tagService.tagsOf(trackId).stream().map(TagResponse::from).toList();
    }

    @PutMapping("/{trackId}/tags/{tagId}")
    public ResponseEntity<Void> attachTag(@PathVariable Long trackId, @PathVariable Long tagId) {
        tagService.attach(trackId, tagId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{trackId}/tags/{tagId}")
    public ResponseEntity<Void> detachTag(@PathVariable Long trackId, @PathVariable Long tagId) {
        tagService.detach(trackId, tagId);
        return ResponseEntity.noContent().build();
    }
}
