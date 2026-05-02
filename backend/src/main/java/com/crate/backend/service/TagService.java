package com.crate.backend.service;

import com.crate.backend.entity.Tag;
import com.crate.backend.entity.Track;
import com.crate.backend.repo.TagRepository;
import com.crate.backend.support.CurrentUser;
import com.crate.backend.support.NotFoundException;
import com.crate.backend.web.dto.TagCreateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class TagService {

    private final TagRepository tags;
    private final TrackService trackService;
    private final CurrentUser currentUser;

    @Transactional(readOnly = true)
    public List<Tag> list() {
        return tags.findAllByOwnerId(currentUser.id());
    }

    @Transactional(readOnly = true)
    public Tag get(Long id) {
        return tags.findById(id)
                .filter(t -> t.getOwnerId().equals(currentUser.id()))
                .orElseThrow(() -> new NotFoundException("tag " + id + " not found"));
    }

    public Tag create(TagCreateRequest req) {
        Tag t = Tag.builder()
                .ownerId(currentUser.id())
                .name(req.name())
                .build();
        return tags.save(t);
    }

    public void delete(Long id) {
        Tag t = get(id);
        tags.delete(t);
    }

    public Track attach(Long trackId, Long tagId) {
        Track track = trackService.get(trackId);
        Tag tag = get(tagId);
        track.getTags().add(tag);
        return track;
    }

    public Track detach(Long trackId, Long tagId) {
        Track track = trackService.get(trackId);
        Tag tag = get(tagId);
        track.getTags().remove(tag);
        return track;
    }

    @Transactional(readOnly = true)
    public List<Tag> tagsOf(Long trackId) {
        return new ArrayList<>(trackService.get(trackId).getTags());
    }
}
