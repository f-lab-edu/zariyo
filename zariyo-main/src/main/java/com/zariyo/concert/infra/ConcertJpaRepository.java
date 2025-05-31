package com.zariyo.concert.infra;

import com.zariyo.concert.domain.entity.Concert;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Optional;

public interface ConcertJpaRepository extends JpaRepository<Concert, Long> {
    
    @Query("SELECT DISTINCT c FROM Concert c " +
           "LEFT JOIN FETCH c.concertHall " +
           "LEFT JOIN FETCH c.concertFiles " +
           "WHERE c.category.categoryId = :categoryId " +
           "AND c.status = 'ACTIVE' " +
           "AND c.endDate >= :currentDate")
    Page<Concert> findByCategoryId(@Param("categoryId") Long categoryId, 
                                   @Param("currentDate") LocalDate currentDate, 
                                   Pageable pageable);
    
    @Query("SELECT DISTINCT c FROM Concert c " +
           "LEFT JOIN FETCH c.concertHall " +
           "LEFT JOIN FETCH c.concertFiles " +
           "WHERE c.status = 'ACTIVE' " +
           "AND c.endDate >= :currentDate")
    Page<Concert> findAllWithFetch(@Param("currentDate") LocalDate currentDate, Pageable pageable);
    
    @Query("SELECT c FROM Concert c " +
           "LEFT JOIN FETCH c.category " +
           "LEFT JOIN FETCH c.concertHall " +
           "WHERE c.concertId = :concertId " +
           "AND c.status = 'ACTIVE'")
    Optional<Concert> findByIdWithDetails(@Param("concertId") Long concertId);
} 