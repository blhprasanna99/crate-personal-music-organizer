package com.crate.backend.repo;

import com.crate.backend.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TagRepository extends JpaRepository<Tag, Long> {

    List<Tag> findAllByOwnerId(Long ownerId);

    Optional<Tag> findByOwnerIdAndName(Long ownerId, String name);
}
