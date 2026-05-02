package com.crate.backend.repo;

import com.crate.backend.entity.Crate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface CrateRepository extends JpaRepository<Crate, Long> {

    List<Crate> findAllByOwnerId(Long ownerId);

    Optional<Crate> findByIdAndOwnerId(Long id, Long ownerId);

    @Query(value = "select * from crate where owner_id = ?1 and deleted_at is not null",
           nativeQuery = true)
    List<Crate> findTrashedByOwnerId(Long ownerId);

    @Modifying
    @Query(value = "update crate set deleted_at = now() where id = ?1 and owner_id = ?2 and deleted_at is null",
           nativeQuery = true)
    int trash(Long id, Long ownerId);

    @Modifying
    @Query(value = "update crate set deleted_at = null where id = ?1 and owner_id = ?2",
           nativeQuery = true)
    int restore(Long id, Long ownerId);

    @Modifying
    @Query(value = "delete from crate where id = ?1 and owner_id = ?2",
           nativeQuery = true)
    int purge(Long id, Long ownerId);
}
