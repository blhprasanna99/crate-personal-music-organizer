package com.crate.backend.web;

import com.crate.backend.service.TagService;
import com.crate.backend.web.dto.TagCreateRequest;
import com.crate.backend.web.dto.TagResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/tags")
@RequiredArgsConstructor
public class TagController {

    private final TagService tags;

    @GetMapping
    public List<TagResponse> list() {
        return tags.list().stream().map(TagResponse::from).toList();
    }

    @GetMapping("/{id}")
    public TagResponse get(@PathVariable Long id) {
        return TagResponse.from(tags.get(id));
    }

    @PostMapping
    public ResponseEntity<TagResponse> create(@Valid @RequestBody TagCreateRequest req) {
        var saved = tags.create(req);
        return ResponseEntity.created(URI.create("/api/tags/" + saved.getId()))
                .body(TagResponse.from(saved));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        tags.delete(id);
        return ResponseEntity.noContent().build();
    }
}
