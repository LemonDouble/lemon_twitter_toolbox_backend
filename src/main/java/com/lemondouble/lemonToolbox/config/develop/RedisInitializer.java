package com.lemondouble.lemonToolbox.config.develop;

import com.lemondouble.lemonToolbox.api.service.RedisLearnMeCountService;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component @Profile({"dev", "test"})
public class RedisInitializer implements InitializingBean {
    private final RedisLearnMeCountService redisLearnMeCountService;

    public RedisInitializer(RedisLearnMeCountService redisLearnMeCountService) {
        this.redisLearnMeCountService = redisLearnMeCountService;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        redisLearnMeCountService.setServiceCountToZero();
    }
}
