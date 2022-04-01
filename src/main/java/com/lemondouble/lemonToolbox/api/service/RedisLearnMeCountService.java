package com.lemondouble.lemonToolbox.api.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;


@Slf4j
@Service
public class RedisLearnMeCountService {

    private final String key = "RedisLearnMeCountService";

    private final RedisTemplate<String, String> redisTemplate;
    private final ValueOperations<String, String> valueOperations;

    public RedisLearnMeCountService(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
        this.valueOperations = redisTemplate.opsForValue();
    }

    public Long increaseAndGetServiceCount(){
        try{
            return valueOperations.increment(key, 1);
        }catch (Exception e){
            throw new RuntimeException("RedisLearnMeCountService.getServiceCount Error! increment 불가합니다!");
        }
    }

    public Long getCurrentServiceCount(){
        try{
            String getCount = valueOperations.get(key);
            return Long.parseLong(getCount);
        }catch (Exception e){
            throw new RuntimeException("RedisLearnMeCountService.getCurrentServiceCount Error!");
        }
    }

    // Test용 메소드
    public void setServiceCountToZero(){
        try{
            valueOperations.set(key, "0");
        }catch (Exception e){
            throw new RuntimeException("RedisLearnMeCountService.setServiceCountToZero Error! set zero 불가능합니다!");
        }
    }
}
