package com.zariyo.concert.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "hall_seats")
public class HallSeat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "hall_seat_id")
    private Long hallSeatId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hall_id")
    private ConcertHall concertHall;

    @Column(name = "seat_grade")
    private String seatGrade;

    @Column(name = "block")
    private String block;

    @Column(name = "seat_row")
    private String seatRow;

    @Column(name = "seat_number")
    private String seatNumber;
}
