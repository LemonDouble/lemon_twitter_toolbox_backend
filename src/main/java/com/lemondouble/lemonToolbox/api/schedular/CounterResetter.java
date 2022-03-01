package com.lemondouble.lemonToolbox.api.schedular;


import com.lemondouble.lemonToolbox.api.service.RedisLearnMeCountService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class CounterResetter {


    private final RedisLearnMeCountService redisLearnMeCountService;

    public CounterResetter(RedisLearnMeCountService redisLearnMeCountService) {
        this.redisLearnMeCountService = redisLearnMeCountService;
    }

    @Transactional
    @Scheduled(cron = "0 0 6 * * *")
    public void counterReset(){
        log.info("counterReset Called");

        redisLearnMeCountService.setServiceCountToZero();
    }
}
