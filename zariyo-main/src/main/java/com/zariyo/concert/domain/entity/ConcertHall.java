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
@Table(name = "CONCERT_HALL")
public class ConcertHall {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "CONCERT_HALL_ID")
    private Long concertHallId;

    @Column(name = "CONCERT_HALL_NAME")
    private String concertHallName;

    @Column(name = "ADDRESS")
    private String address;

    @Column(name = "TOTAL_SEATS")
    private int totalSeats;

}
