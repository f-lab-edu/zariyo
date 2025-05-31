package com.zariyo.concert.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "concerts")
@EntityListeners(AuditingEntityListener.class)
public class Concert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "concert_id")
    private Long concertId;

    @Column(name = "title")
    private String title;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hall_id")
    private ConcertHall concertHall;

    @Column(name = "age_limit")
    private String ageLimit;

    @Column(name = "description")
    private String description;

    @Column(name = "running_time")
    private Integer runningTime;

    @Column(name = "reservation_count")
    @Builder.Default
    private Integer reservationCount = 0;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    @Builder.Default
    private ConcertStatus status = ConcertStatus.ACTIVE;

    @OneToMany(mappedBy = "concert", fetch = FetchType.LAZY)
    private List<ConcertFile> concertFiles;

    @OneToMany(mappedBy = "concert", fetch = FetchType.LAZY)
    private List<SeatPrice> seatPrices;

    @OneToMany(mappedBy = "concert", fetch = FetchType.LAZY)
    private List<Schedule> schedules;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public enum ConcertStatus {
        ACTIVE, ENDED, CANCELLED
    }
}
