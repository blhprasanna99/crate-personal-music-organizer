package com.crate.backend.service;

import com.crate.backend.entity.Crate;
import com.crate.backend.entity.Track;
import com.crate.backend.repo.CrateRepository;
import com.crate.backend.support.CurrentUser;
import com.crate.backend.support.NotFoundException;
import com.crate.backend.web.dto.CrateCreateRequest;
import com.crate.backend.web.dto.CrateUpdateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CrateService {

    private final CrateRepository crates;
    private final TrackService trackService;

    private final CurrentUser currentUser;

    @Transactional(readOnly = true)
    public List<Crate> list() {
        return crates.findAllByOwnerId(currentUser.id());
    }

    @Transactional(readOnly = true)
    public Crate get(Long id) {
        return crates.findByIdAndOwnerId(id, currentUser.id())
                .orElseThrow(() -> new NotFoundException("crate " + id + " not found"));
    }

    @Transactional(readOnly = true)
    public List<Track> tracksIn(Long crateId) {
        return new ArrayList<>(get(crateId).getTracks());
    }

    public Crate create(CrateCreateRequest req) {
        Crate c = Crate.builder()
                .ownerId(currentUser.id())
                .name(req.name())
                .description(req.description())
                .build();
        return crates.save(c);
    }

    public Crate update(Long id, CrateUpdateRequest req) {
        Crate c = get(id);
        if (req.name() != null) c.setName(req.name());
        if (req.description() != null) c.setDescription(req.description());
        return c;
    }

    public void delete(Long id) {
        int rows = crates.trash(id, currentUser.id());
        if (rows == 0) throw new NotFoundException("crate " + id + " not found");
    }

    public Crate addTrack(Long crateId, Long trackId) {
        Crate c = get(crateId);
        Track t = trackService.get(trackId);
        c.getTracks().add(t);
        return c;
    }

    public Crate removeTrack(Long crateId, Long trackId) {
        Crate c = get(crateId);
        Track t = trackService.get(trackId);
        c.getTracks().remove(t);
        return c;
    }

    @Transactional(readOnly = true)
    public List<Crate> listTrashed() {
        return crates.findTrashedByOwnerId(currentUser.id());
    }

    public void restore(Long id) {
        int rows = crates.restore(id, currentUser.id());
        if (rows == 0) throw new NotFoundException("crate " + id + " not found in trash");
    }

    public void purge(Long id) {
        int rows = crates.purge(id, currentUser.id());
        if (rows == 0) throw new NotFoundException("crate " + id + " not found");
    }
}
