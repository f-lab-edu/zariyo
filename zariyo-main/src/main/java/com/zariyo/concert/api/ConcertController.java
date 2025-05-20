package com.zariyo.concert.api;

import com.zariyo.concert.api.request.ReserveSeatRequest;
import com.zariyo.concert.api.response.*;
import com.zariyo.concert.application.dto.ConcertSeatDto;
import com.zariyo.concert.application.facade.ConcertFacade;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/concerts")
@RequiredArgsConstructor
public class ConcertController {

    private final ConcertFacade concertFacade;

    @Operation(
            summary = "콘서트 상세 정보 조회",
            description = "콘서트 정보(콘서트명, 장소, 콘서트 회차 및 일자, 좌석 등급별 가격, 공연 설명) 조회 API",
            responses = {
                    @ApiResponse(responseCode = "200", description = "콘서트 정보 조회 성공",
                            content = @Content(mediaType = "application/json",
                                    examples = @ExampleObject(
                                            value = """
                                            {
                                              "concert": {
                                                    "concertId": 1,
                                                    "concertName": "zariyo 콘서트",
                                                    "address": "서울특별시 송파구 올림픽로 424"
                                               },
                                              "schedules": [
                                                {
                                                  "scheduleId": 1,
                                                  "concertDate": "2025-05-01 09:00"
                                                },
                                                {
                                                  "scheduleId": 2,
                                                  "concertDate": "2025-05-01 14:00"
                                                }
                                              ],
                                              "gradePrices": [
                                                {
                                                  "grade": "VIP",
                                                  "price": 200000
                                                },
                                                {
                                                  "grade": "R",
                                                  "price": 150000
                                                },
                                                {
                                                  "grade": "S",
                                                  "price": 100000
                                                }
                                              ],
                                              "files": [
                                                {
                                                  "fileId": 1,
                                                  "fileName": "concert.jpg",
                                                  "filePath": "/concerts/1/concert.jpg",
                                                  "fileType": "POSTER"
                                                },
                                                {
                                                  "fileId": 2,
                                                  "fileName": "poster.jpg",
                                                  "filePath": "/concerts/1/poster.jpg",
                                                  "fileType": "DESCRIPTION"
                                                }
                                              ]
                                            }
                                            """
                                    )
                            )
                    )
            }
    )
    @GetMapping("/{concertId}")
    public ResponseEntity<ConcertInfoResponse> getConcertInfo(@PathVariable("concertId") long concertId) {
        return ResponseEntity.ok(concertFacade.getConcertInfo(concertId));
    }

    @Operation(
            summary = "콘서트 좌석 예약",
            description = "콘서트 좌석 예약 API, 좌석 예약 성공 시 좌석 및 콘서트 정보 반환, 실패 시 에러 메시지 반환",
            responses = {
                    @ApiResponse(responseCode = "200", description = "콘서트 좌석 예약 성공 - 결제페이지 이동",
                            content = @Content(mediaType = "application/json",
                                    examples = @ExampleObject(
                                            value = """
                                            {
                                                "reservationId": 1,
                                                "concert":
                                                {
                                                    "concertId": 1,
                                                    "concertName": "zariyo 콘서트",
                                                    "address": "서울특별시 송파구 올림픽로 424"
                                                },
                                                "schedule":
                                                {
                                                  "scheduleId": 1,
                                                  "concertDate": "2025-05-01 09:00"
                                                },
                                                "seats": [
                                                    {
                                                        "scheduleSeatId": 1,
                                                        "block": "A",
                                                        "seatNumber": "2",
                                                        "grade": "VIP",
                                                        "price": 200000
                                                    },
                                                    {
                                                        "scheduleSeatId": 2,
                                                        "block": "A",
                                                        "seatNumber": "2",
                                                        "grade": "VIP",
                                                        "price": 200000
                                                    }
                                                ]
                                            }
                                            """
                                    )
                            )
                    ),
                    @ApiResponse(responseCode = "400", description = "예약 실패 - 이미 예약된 좌석",
                            content = @Content(mediaType = "application/json",
                                    examples = @ExampleObject(
                                            value = """
                                            {
                                              "message": "이미 예약된 좌석이 포함되어 있습니다."
                                            }
                                            """
                                    )
                            )
                    )
            }
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "예약할 좌석 ID 목록",
            required = true,
            content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(value = "[1, 2]")
            )
    )
    @PostMapping("/reserve")
    public ResponseEntity<ReservationResponse> reserveConcertSeats(@RequestBody ReserveSeatRequest reserveSeats) {
        return ResponseEntity.ok(concertFacade.reserveConcertSeats(reserveSeats));
    }


    @Operation(
            summary = "콘서트 좌석 조회",
            description = "콘서트 예약시 좌석리스트 조회 API (회차 좌석 ID, 좌석구역, 좌석번호, 좌석등급, 가격)",
            responses = {
                    @ApiResponse(responseCode = "200", description = "좌석 리스트 조회 성공",
                            content = @Content(mediaType = "application/json",
                                    examples = @ExampleObject(
                                            value = """
                                            [
                                              {
                                                "scheduleSeatId": 1,
                                                "block": "A",
                                                "seatNumber": "1",
                                                "grade": "VIP",
                                                "price": 200000
                                              },
                                              {
                                                "scheduleSeatId": 2,
                                                "block": "A",
                                                "seatNumber": "2",
                                                "grade": "VIP",
                                                "price": 200000
                                              },
                                              {
                                                "scheduleSeatId": 3,
                                                "block": "A",
                                                "seatNumber": "3",
                                                "grade": "VIP",
                                                "price": 200000
                                              }
                                            ]
                                            """
                                    )
                            )
                    )
            }
    )
    @GetMapping("/{scheduleId}/seats")
    public ResponseEntity<List<ConcertSeatDto>> getAvailableConcertSeats(@PathVariable("scheduleId") long scheduleId) {
        return ResponseEntity.ok(concertFacade.getAvailableConcertSeats(scheduleId));
    }

}
