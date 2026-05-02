package com.crate.backend.service;

import com.crate.backend.entity.Track;
import com.crate.backend.repo.TrackRepository;
import com.crate.backend.support.CurrentUser;
import com.crate.backend.web.dto.Filter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FilterEngine {

    private final TrackRepository tracks;
    private final CurrentUser currentUser;

    public List<Track> resolve(List<Filter> criteria) {
        Specification<Track> spec = ownedByCurrentUser();
        if (criteria != null) {
            for (Filter f : criteria) {
                spec = spec.and(toSpec(f));
            }
        }
        return tracks.findAll(spec);
    }

    private Specification<Track> ownedByCurrentUser() {
        Long ownerId = currentUser.id();
        return (root, q, cb) -> cb.equal(root.get("ownerId"), ownerId);
    }

    private Specification<Track> toSpec(Filter f) {
        return switch (f.op()) {
            case "tag"             -> hasTag(asString(f.value()));
            case "noTag"           -> hasNoTag();
            case "artistEq"        -> artistEq(asString(f.value()));
            case "albumEq"         -> albumEq(asString(f.value()));
            case "titleContains"   -> titleContains(asString(f.value()));
            case "addedWithinDays" -> addedWithinDays(asInt(f.value()));
            default -> throw new IllegalArgumentException("unknown filter op: " + f.op());
        };
    }

    private Specification<Track> hasTag(String tagName) {
        return (root, query, cb) -> {
            if (query.getResultType() == Track.class) query.distinct(true);
            var tags = root.join("tags");
            return cb.equal(tags.get("name"), tagName);
        };
    }

    private Specification<Track> hasNoTag() {
        return (root, query, cb) -> cb.isEmpty(root.get("tags"));
    }

    private Specification<Track> artistEq(String value) {
        return (root, query, cb) -> cb.equal(root.get("artist"), value);
    }

    private Specification<Track> albumEq(String value) {
        return (root, query, cb) -> cb.equal(root.get("album"), value);
    }

    private Specification<Track> titleContains(String value) {
        String pattern = "%" + value.toLowerCase() + "%";
        return (root, query, cb) -> cb.like(cb.lower(root.get("title")), pattern);
    }

    private Specification<Track> addedWithinDays(int days) {
        OffsetDateTime cutoff = OffsetDateTime.now().minusDays(days);
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("createdAt"), cutoff);
    }

    private static String asString(Object v) {
        if (v instanceof String s) return s;
        throw new IllegalArgumentException("expected string, got " + v);
    }

    private static int asInt(Object v) {
        if (v instanceof Number n) return n.intValue();
        throw new IllegalArgumentException("expected number, got " + v);
    }
}
