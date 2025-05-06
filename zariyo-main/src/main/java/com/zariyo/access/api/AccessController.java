package com.zariyo.access.api;

import com.zariyo.access.api.dto.AccessDto;
import com.zariyo.access.service.AccessService;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/access")
@RequiredArgsConstructor
public class AccessController {

    private final AccessService accessService;

    @GetMapping("/token")
    public ResponseEntity<AccessDto> enterOrEnqueue(){
        return ResponseEntity.ok(accessService.handleThresholdAndRegister());
    }

    @GetMapping("/status")
    public ResponseEntity<AccessDto> checkMainStatus(@RequestParam @NotBlank String token){
        return ResponseEntity.ok(accessService.getCurrentStatusToken(token));
    }
}
