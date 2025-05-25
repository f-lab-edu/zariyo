package com.zariyo.concert.domain.repository;

import com.zariyo.concert.domain.entity.Concert;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface ConcertRepository {
    
    Page<Concert> findAll(Pageable pageable);
    
    Page<Concert> findByCategoryId(Long categoryId, Pageable pageable);

    Optional<Concert> findByIdWithDetails(Long concertId);
}