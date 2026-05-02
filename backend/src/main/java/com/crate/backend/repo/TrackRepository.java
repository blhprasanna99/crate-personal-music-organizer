package com.crate.backend.repo;

import com.crate.backend.entity.Track;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TrackRepository extends JpaRepository<Track, Long> {

    List<Track> findAllByOwnerId(Long ownerId);

    Optional<Track> findByIdAndOwnerId(Long id, Long ownerId);

    Optional<Track> findByOwnerIdAndFilePath(Long ownerId, String filePath);
}
