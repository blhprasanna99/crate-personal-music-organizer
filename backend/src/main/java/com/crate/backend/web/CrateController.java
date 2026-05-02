package com.crate.backend.web;

import com.crate.backend.service.CrateGraphService;
import com.crate.backend.service.CrateService;
import com.crate.backend.web.dto.CrateCreateRequest;
import com.crate.backend.web.dto.CrateResponse;
import com.crate.backend.web.dto.CrateUpdateRequest;
import com.crate.backend.web.dto.TrackResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/crates")
@RequiredArgsConstructor
public class CrateController {

    private final CrateService crates;
    private final CrateGraphService graph;

    @GetMapping
    public List<CrateResponse> list(
            @RequestParam(defaultValue = "false") boolean trashed
    ) {
        var list = trashed ? crates.listTrashed() : crates.list();
        return list.stream().map(CrateResponse::from).toList();
    }

    @GetMapping("/{id}")
    public CrateResponse get(@PathVariable Long id) {
        return CrateResponse.from(crates.get(id));
    }

    @PostMapping
    public ResponseEntity<CrateResponse> create(@Valid @RequestBody CrateCreateRequest req) {
        var saved = crates.create(req);
        return ResponseEntity.created(URI.create("/api/crates/" + saved.getId()))
                .body(CrateResponse.from(saved));
    }

    @PatchMapping("/{id}")
    public CrateResponse update(@PathVariable Long id, @RequestBody CrateUpdateRequest req) {
        return CrateResponse.from(crates.update(id, req));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            @RequestParam(defaultValue = "trash") String mode
    ) {
        switch (mode) {
            case "trash" -> crates.delete(id);
            case "purge" -> crates.purge(id);
            default -> throw new IllegalArgumentException("mode must be 'trash' or 'purge'");
        }
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/restore")
    public ResponseEntity<Void> restore(@PathVariable Long id) {
        crates.restore(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/tracks")
    public List<TrackResponse> tracks(
            @PathVariable Long id,
            @RequestParam(defaultValue = "false") boolean recursive
    ) {
        var tracks = recursive ? graph.effectiveTracks(id) : crates.tracksIn(id);
        return tracks.stream().map(TrackResponse::from).toList();
    }

    @PutMapping("/{crateId}/tracks/{trackId}")
    public ResponseEntity<Void> addTrack(@PathVariable Long crateId, @PathVariable Long trackId) {
        crates.addTrack(crateId, trackId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{crateId}/tracks/{trackId}")
    public ResponseEntity<Void> removeTrack(@PathVariable Long crateId, @PathVariable Long trackId) {
        crates.removeTrack(crateId, trackId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{childId}/parents/{parentId}")
    public ResponseEntity<Void> addParent(@PathVariable Long childId, @PathVariable Long parentId) {
        graph.addParent(childId, parentId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{childId}/parents/{parentId}")
    public ResponseEntity<Void> removeParent(@PathVariable Long childId, @PathVariable Long parentId) {
        graph.removeParent(childId, parentId);
        return ResponseEntity.noContent().build();
    }
}
