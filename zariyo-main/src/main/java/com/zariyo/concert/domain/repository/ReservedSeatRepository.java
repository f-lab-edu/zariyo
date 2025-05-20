package com.zariyo.concert.domain.repository;

import com.zariyo.concert.domain.entity.ReservedSeat;

import java.util.List;

public interface ReservedSeatRepository {
    void saveReserveSeats(List<ReservedSeat> reservedSeat);
}
