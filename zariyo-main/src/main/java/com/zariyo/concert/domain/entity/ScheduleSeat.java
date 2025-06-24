package com.zariyo.concert.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "schedule_seats")
public class ScheduleSeat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "schedule_seat_id")
    private Long scheduleSeatId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "schedule_id")
    private Schedule schedule;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hall_seat_id")
    private HallSeat hallSeat;

    @Column(name = "seat_grade")
    private String seatGrade;

    @Column(name = "price")
    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    @Builder.Default
    private SeatStatus status = SeatStatus.AVAILABLE;

    public enum SeatStatus {
        AVAILABLE, PENDING, RESERVED
    }

    /**
     * 좌석 예약 처리
     */
    public void reserve() {
        this.status = SeatStatus.RESERVED;
    }

    /**
     * 좌석 선점 처리
     */
    public void hold() {
        this.status = SeatStatus.PENDING;
    }

    /**
     * 좌석 해제 (사용 가능 상태로 복구)
     */
    public void release() {
        this.status = SeatStatus.AVAILABLE;
    }
}
