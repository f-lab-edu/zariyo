package com.zariyo.concert.application.service;

import com.zariyo.common.category.CategoryType;
import com.zariyo.common.exception.ErrorCode;
import com.zariyo.common.exception.custom.DuplicateSeatReservationException;
import com.zariyo.concert.api.request.ReserveSeatRequest;
import com.zariyo.concert.domain.entity.Category;
import com.zariyo.concert.domain.entity.ReservedSeat;
import com.zariyo.concert.domain.entity.Schedule;
import com.zariyo.concert.domain.entity.ScheduleSeatList;
import com.zariyo.concert.domain.repository.ReservedSeatRepository;
import com.zariyo.reservation.domain.entity.Reservation;
import com.zariyo.reservation.domain.repository.ReservationRepository;
import com.zariyo.user.domain.entity.User;
import com.zariyo.user.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReserveConcertService {

    private final ReservedSeatRepository reservedSeatRepository;
    private final ReservationRepository reservationRepository;

    @Transactional
    public Long reserveConcertSeats(ReserveSeatRequest reserveSeats) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
        Long userId = userDetails.getUserId();

        User user = User.builder().userId(userId).build();
        Category category = Category.builder().categoryId(CategoryType.CONCERT.getId()).categoryName(CategoryType.CONCERT.getName()).build();

        Long reservationId = reservationRepository.concertReservationSave(Reservation.builder()
                .user(user)
                .category(category)
                .build()
        );

        try{
            reservedSeatRepository.saveReserveSeats(reserveSeats.getScheduleSeatIds().stream()
                    .sorted()
                    .map(seatId -> ReservedSeat.builder()
                            .schedule(Schedule.builder().scheduleId(reserveSeats.getScheduleId()).build())
                            .reservation(Reservation.builder().reservationId(reservationId).build())
                            .category(category)
                            .scheduleSeat(ScheduleSeatList.builder().scheduleSeatId(seatId).build())
                            .build())
                    .toList());
        } catch (DataIntegrityViolationException e) {
            throw new DuplicateSeatReservationException(ErrorCode.SEAT_ALREADY_RESERVED);
        }
        return reservationId;
    }
}
