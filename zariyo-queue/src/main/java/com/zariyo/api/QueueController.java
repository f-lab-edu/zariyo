package com.zariyo.api;

import com.zariyo.api.dto.QueueDto;
import com.zariyo.service.QueueService;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/queue")
@RequiredArgsConstructor
@Validated
public class QueueController {

    private final QueueService queueService;

    /**
     * 서버의 RPS 기준으로 대기열 진입 여부를 결정하고 토큰을 발급
     *
     * @return QueueDto - 대기열 상태(WAITING or OPEN) 및 토큰 정보
     *                    WAITING일 경우 현재 순번 포함
     */
    @GetMapping("/token")
    public ResponseEntity<QueueDto> enterQueue() {
        return ResponseEntity.ok(queueService.registerToQueue());
    }

    /**
     * WebSocket 연결이 불가한 상황을 대비한 HTTP 폴링용 백업 API
     *
     * @param token 사용자 식별 토큰
     * @return QueueDto - 대기열 상태(WAITING or OPEN), 현재 순번 및 토큰 정보
     */
    @GetMapping("/position")
    public ResponseEntity<QueueDto> pollQueueStatus(@RequestParam @NotBlank String token) {
        int position = queueService.getPosition(token);
        if(position <= 10) {
            queueService.removeFromQueue(token);
            return ResponseEntity.ok(QueueDto.open(token));
        }

        return ResponseEntity.ok(QueueDto.waiting(token, position));
    }

}
