package com.crate.backend.repo;

import com.crate.backend.entity.Crate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CrateRepository extends JpaRepository<Crate, Long> {

    List<Crate> findAllByOwnerId(Long ownerId);

    Optional<Crate> findByIdAndOwnerId(Long id, Long ownerId);
}
