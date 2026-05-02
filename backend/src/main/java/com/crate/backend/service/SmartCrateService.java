package com.crate.backend.service;

import com.crate.backend.entity.SmartCrate;
import com.crate.backend.entity.Track;
import com.crate.backend.repo.SmartCrateRepository;
import com.crate.backend.support.CurrentUser;
import com.crate.backend.support.NotFoundException;
import com.crate.backend.web.dto.SmartCrateCreateRequest;
import com.crate.backend.web.dto.SmartCrateUpdateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class SmartCrateService {

    private final SmartCrateRepository smartCrates;
    private final FilterEngine filterEngine;
    private final CurrentUser currentUser;

    @Transactional(readOnly = true)
    public List<SmartCrate> list() {
        return smartCrates.findAllByOwnerId(currentUser.id());
    }

    @Transactional(readOnly = true)
    public SmartCrate get(Long id) {
        return smartCrates.findByIdAndOwnerId(id, currentUser.id())
                .orElseThrow(() -> new NotFoundException("smart crate " + id + " not found"));
    }

    public SmartCrate create(SmartCrateCreateRequest req) {
        return smartCrates.save(SmartCrate.builder()
                .ownerId(currentUser.id())
                .name(req.name())
                .description(req.description())
                .criteria(req.criteria())
                .build());
    }

    public SmartCrate update(Long id, SmartCrateUpdateRequest req) {
        SmartCrate s = get(id);
        if (req.name() != null) s.setName(req.name());
        if (req.description() != null) s.setDescription(req.description());
        if (req.criteria() != null) s.setCriteria(req.criteria());
        return s;
    }

    @Transactional(readOnly = true)
    public List<Track> resolve(Long id) {
        SmartCrate s = get(id);
        return filterEngine.resolve(s.getCriteria());
    }

    public void delete(Long id) {
        int rows = smartCrates.trash(id, currentUser.id());
        if (rows == 0) throw new NotFoundException("smart crate " + id + " not found");
    }

    @Transactional(readOnly = true)
    public List<SmartCrate> listTrashed() {
        return smartCrates.findTrashedByOwnerId(currentUser.id());
    }

    public void restore(Long id) {
        int rows = smartCrates.restore(id, currentUser.id());
        if (rows == 0) throw new NotFoundException("smart crate " + id + " not found in trash");
    }

    public void purge(Long id) {
        int rows = smartCrates.purge(id, currentUser.id());
        if (rows == 0) throw new NotFoundException("smart crate " + id + " not found");
    }
}
