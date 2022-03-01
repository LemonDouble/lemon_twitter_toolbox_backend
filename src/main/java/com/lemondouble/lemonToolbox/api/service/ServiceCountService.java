package com.lemondouble.lemonToolbox.api.service;

import com.lemondouble.lemonToolbox.api.dto.RegisteredService.LearnMeCanUseResponseDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class ServiceCountService {

    private final RedisLearnMeCountService redisLearnMeCountService;

    @Value("${service-limit.learnme}")
    private Long LEARNME_LIMIT;

    public ServiceCountService(RedisLearnMeCountService redisLearnMeCountService) {
        this.redisLearnMeCountService = redisLearnMeCountService;
    }


    public LearnMeCanUseResponseDto canUseLearnMeService(){
        Long currentServiceCount = redisLearnMeCountService.getCurrentServiceCount();

        Boolean canUse = currentServiceCount <= LEARNME_LIMIT;
        Long registerCount = canUse ? currentServiceCount : LEARNME_LIMIT;

        return LearnMeCanUseResponseDto.builder()
                .canUse(canUse)
                .registerCount(registerCount)
                .registerLimit(LEARNME_LIMIT)
                .build();
    }

}
