package com.crate.backend.service;

import com.crate.backend.entity.Track;
import com.crate.backend.repo.TrackRepository;
import com.crate.backend.support.CurrentUser;
import com.crate.backend.support.NotFoundException;
import com.crate.backend.web.dto.TrackCreateRequest;
import com.crate.backend.web.dto.TrackUpdateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class TrackService {

    private final TrackRepository tracks;
    private final CurrentUser currentUser;

    @Transactional(readOnly = true)
    public List<Track> list() {
        return tracks.findAllByOwnerId(currentUser.id());
    }

    @Transactional(readOnly = true)
    public Track get(Long id) {
        return tracks.findByIdAndOwnerId(id, currentUser.id())
                .orElseThrow(() -> new NotFoundException("track " + id + " not found"));
    }

    public Track create(TrackCreateRequest req) {
        Track t = Track.builder()
                .ownerId(currentUser.id())
                .filePath(req.filePath())
                .title(req.title())
                .artist(req.artist())
                .album(req.album())
                .durationMs(req.durationMs())
                .build();
        return tracks.save(t);
    }

    public Track update(Long id, TrackUpdateRequest req) {
        Track t = get(id);
        if (req.title() != null) t.setTitle(req.title());
        if (req.artist() != null) t.setArtist(req.artist());
        if (req.album() != null) t.setAlbum(req.album());
        if (req.durationMs() != null) t.setDurationMs(req.durationMs());
        return t;
    }

    public void delete(Long id) {
        int rows = tracks.trash(id, currentUser.id());
        if (rows == 0) throw new NotFoundException("track " + id + " not found");
    }

    @Transactional(readOnly = true)
    public List<Track> listTrashed() {
        return tracks.findTrashedByOwnerId(currentUser.id());
    }

    public void restore(Long id) {
        int rows = tracks.restore(id, currentUser.id());
        if (rows == 0) throw new NotFoundException("track " + id + " not found in trash");
    }

    public void purge(Long id) {
        int rows = tracks.purge(id, currentUser.id());
        if (rows == 0) throw new NotFoundException("track " + id + " not found");
    }
}
