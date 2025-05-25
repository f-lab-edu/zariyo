package com.zariyo.concert.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "concert_halls")
public class ConcertHall {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "hall_id")
    private Long hallId;

    @Column(name = "hall_name", nullable = false, length = 100)
    private String hallName;

    @Column(name = "address")
    private String address;

    @OneToMany(mappedBy = "concertHall", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<HallSeat> hallSeats;
}
