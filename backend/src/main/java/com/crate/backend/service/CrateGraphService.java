package com.crate.backend.service;

import com.crate.backend.entity.Track;
import com.crate.backend.repo.TrackRepository;
import com.crate.backend.support.CurrentUser;
import com.crate.backend.support.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CrateGraphService {

    private final JdbcClient jdbc;
    private final CrateService crateService;
    private final TrackRepository trackRepo;
    private final CurrentUser currentUser;

    public void addParent(Long childId, Long parentId) {
        if (childId.equals(parentId)) {
            throw new IllegalArgumentException("a crate cannot be its own parent");
        }
        crateService.get(childId);
        crateService.get(parentId);

        if (createsCycle(childId, parentId)) {
            throw new IllegalArgumentException(
                    "adding parent " + parentId + " to crate " + childId + " would create a cycle");
        }

        jdbc.sql("""
                insert into crate_parent (child_id, parent_id)
                values (?, ?)
                on conflict do nothing
                """)
                .params(childId, parentId)
                .update();
    }

    public void removeParent(Long childId, Long parentId) {
        crateService.get(childId);
        crateService.get(parentId);

        int rows = jdbc.sql("delete from crate_parent where child_id = ? and parent_id = ?")
                .params(childId, parentId)
                .update();

        if (rows == 0) {
            throw new NotFoundException("edge " + parentId + " -> " + childId + " not found");
        }
    }

    @Transactional(readOnly = true)
    public List<Track> effectiveTracks(Long crateId) {
        crateService.get(crateId);

        List<Long> trackIds = jdbc.sql("""
                with recursive descendants(id) as (
                    select id from crate
                    where id = ? and owner_id = ? and deleted_at is null
                    union
                    select cp.child_id
                    from crate_parent cp
                    join descendants d on cp.parent_id = d.id
                    join crate c on c.id = cp.child_id and c.deleted_at is null
                )
                select distinct ct.track_id
                from crate_track ct
                join track t on t.id = ct.track_id and t.deleted_at is null
                where ct.crate_id in (select id from descendants)
                """)
                .params(crateId, currentUser.id())
                .query(Long.class)
                .list();

        return trackIds.isEmpty() ? List.of() : trackRepo.findAllById(trackIds);
    }

    private boolean createsCycle(Long childId, Long parentId) {
        return Boolean.TRUE.equals(jdbc.sql("""
                with recursive ancestors(id) as (
                    select parent_id from crate_parent where child_id = ?
                    union
                    select cp.parent_id
                    from crate_parent cp
                    join ancestors a on cp.child_id = a.id
                )
                select exists(select 1 from ancestors where id = ?)
                """)
                .params(parentId, childId)
                .query(Boolean.class)
                .single());
    }
}
