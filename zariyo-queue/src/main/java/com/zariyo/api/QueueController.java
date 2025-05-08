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
     * 대기열 생성 여부 결정
     * @return QueueDto - OPEN(즉시 입장) 또는 WAITING(대기열 진입)
     */
    @GetMapping("/token")
    public ResponseEntity<QueueDto> enterOrEnqueue() {
        return ResponseEntity.ok(queueService.enterOrEnqueue());
    }

    /**
     * 토큰 유효성 검사 및 대기열 순번, 입장 가능 여부 확인 (10초 주기 폴링)
     *
     * @param token 발급받은 토큰 (NotBlank)
     * @param entryNumber 대기열 진입 순번
     * @return QueueDto - 현재 대기열 상태 정보
     */
    @GetMapping("/status")
    public ResponseEntity<QueueDto> checkQueueStatus(@RequestParam @NotBlank String token, @RequestParam int entryNumber) {
        return ResponseEntity.ok(queueService.getQueueStatusAndPosition(token, entryNumber));
    }
}