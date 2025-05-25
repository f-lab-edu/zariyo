package com.zariyo.concert.infra;

import com.zariyo.concert.domain.entity.Concert;
import com.zariyo.concert.domain.repository.ConcertRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ConcertRepositoryImpl implements ConcertRepository {
    
    private final ConcertJpaRepository concertJpaRepository;
    
    @Override
    public Page<Concert> findAll(Pageable pageable) {
        return concertJpaRepository.findAllWithFetch(LocalDate.now(), pageable);
    }
    
    @Override
    public Page<Concert> findByCategoryId(Long categoryId, Pageable pageable) {
        return concertJpaRepository.findByCategoryId(categoryId, LocalDate.now(), pageable);
    }
    
    @Override
    public Optional<Concert> findByIdWithDetails(Long concertId) {
        return concertJpaRepository.findByIdWithDetails(concertId);
    }
}