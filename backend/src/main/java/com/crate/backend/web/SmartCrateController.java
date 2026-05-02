package com.crate.backend.web;

import com.crate.backend.service.SmartCrateService;
import com.crate.backend.web.dto.SmartCrateCreateRequest;
import com.crate.backend.web.dto.SmartCrateResponse;
import com.crate.backend.web.dto.SmartCrateUpdateRequest;
import com.crate.backend.web.dto.TrackResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/smart-crates")
@RequiredArgsConstructor
public class SmartCrateController {

    private final SmartCrateService smartCrates;

    @GetMapping
    public List<SmartCrateResponse> list(
            @RequestParam(defaultValue = "false") boolean trashed
    ) {
        var list = trashed ? smartCrates.listTrashed() : smartCrates.list();
        return list.stream().map(SmartCrateResponse::from).toList();
    }

    @GetMapping("/{id}")
    public SmartCrateResponse get(@PathVariable Long id) {
        return SmartCrateResponse.from(smartCrates.get(id));
    }

    @PostMapping
    public ResponseEntity<SmartCrateResponse> create(@Valid @RequestBody SmartCrateCreateRequest req) {
        var saved = smartCrates.create(req);
        return ResponseEntity.created(URI.create("/api/smart-crates/" + saved.getId()))
                .body(SmartCrateResponse.from(saved));
    }

    @PatchMapping("/{id}")
    public SmartCrateResponse update(@PathVariable Long id, @RequestBody SmartCrateUpdateRequest req) {
        return SmartCrateResponse.from(smartCrates.update(id, req));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            @RequestParam(defaultValue = "trash") String mode
    ) {
        switch (mode) {
            case "trash" -> smartCrates.delete(id);
            case "purge" -> smartCrates.purge(id);
            default -> throw new IllegalArgumentException("mode must be 'trash' or 'purge'");
        }
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/restore")
    public ResponseEntity<Void> restore(@PathVariable Long id) {
        smartCrates.restore(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/tracks")
    public List<TrackResponse> tracks(@PathVariable Long id) {
        return smartCrates.resolve(id).stream().map(TrackResponse::from).toList();
    }
}
