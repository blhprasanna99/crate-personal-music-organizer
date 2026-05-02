package com.crate.backend.repo;

import com.crate.backend.entity.Track;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface TrackRepository extends JpaRepository<Track, Long> {

    List<Track> findAllByOwnerId(Long ownerId);

    Optional<Track> findByIdAndOwnerId(Long id, Long ownerId);

    Optional<Track> findByOwnerIdAndFilePath(Long ownerId, String filePath);

    @Query(value = "select * from track where owner_id = ?1 and deleted_at is not null",
           nativeQuery = true)
    List<Track> findTrashedByOwnerId(Long ownerId);

    @Modifying
    @Query(value = "update track set deleted_at = now() where id = ?1 and owner_id = ?2 and deleted_at is null",
           nativeQuery = true)
    int trash(Long id, Long ownerId);

    @Modifying
    @Query(value = "update track set deleted_at = null where id = ?1 and owner_id = ?2",
           nativeQuery = true)
    int restore(Long id, Long ownerId);

    @Modifying
    @Query(value = "delete from track where id = ?1 and owner_id = ?2",
           nativeQuery = true)
    int purge(Long id, Long ownerId);
}
