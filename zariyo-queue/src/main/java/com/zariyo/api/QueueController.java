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
     * 대기열 토큰 추가
     * @return QueueDto - OPEN(즉시 입장) 또는 WAITING(대기열 진입)
     */
    @PostMapping("/token")
    public ResponseEntity<QueueDto> enqueueToken(@RequestParam @NotBlank String token) {
        return ResponseEntity.ok(queueService.enqueueToken(token));
    }

    @GetMapping("/position")
    public ResponseEntity<QueueDto> checkQueuePosition(@RequestParam int entryNumber){
        return ResponseEntity.ok(queueService.getcheckQueueMyPosition(entryNumber));
    }

}