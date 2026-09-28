package com.booking.repository;


import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.booking.entity.Resource;

import jakarta.persistence.LockModeType;

public interface ResourceRepository extends JpaRepository<Resource, Long> {
	

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT r
        FROM Resource r
        WHERE r.id = :id
        """)
    Optional<Resource> findByIdForUpdate(
            @Param("id") Long id
    );

}