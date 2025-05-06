package com.zariyo.access.service;

import com.zariyo.access.api.dto.AccessDto;
import com.zariyo.access.infra.MainRedisRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AccessService {

    private final MainRedisRepository mainRedisRepository;

    public AccessDto handleThresholdAndRegister(){
        String token = UUID.randomUUID().toString();
        if(mainRedisRepository.addToOpenSet(token)) return AccessDto.open(token);
        return AccessDto.waiting(token);
    }

    public AccessDto getCurrentStatusToken(String token) {
        if(mainRedisRepository.getCurrentStatusToken(token) != null){
            return AccessDto.open(token);
        }
        return AccessDto.waiting(token);
    }

    public void queueTokenToMainSet(String token) {
        mainRedisRepository.queueTokenToMainSet(token);
    }
}
