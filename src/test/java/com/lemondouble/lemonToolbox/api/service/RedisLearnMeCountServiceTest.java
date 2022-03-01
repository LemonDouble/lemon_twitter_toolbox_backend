package com.lemondouble.lemonToolbox.api.service;

import com.lemondouble.lemonToolbox.api.repository.entity.OAuthToken;
import com.lemondouble.lemonToolbox.config.LocalStackSqsConfig;
import com.lemondouble.lemonToolbox.config.RedisTestContainerInitializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.web.server.ResponseStatusException;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = LocalStackSqsConfig.class)
@ContextConfiguration(initializers = {RedisTestContainerInitializer.class})
class RedisLearnMeCountServiceTest {

    @Autowired
    RedisLearnMeCountService redisLearnMeCountService;

    @AfterEach
    public void clear(){
        redisLearnMeCountService.setServiceCountToZero();
    }

    @Test
    public void Thread_Safe확인() throws InterruptedException {
        //given

        //when

        int numberOfExcute = 300;
        ExecutorService service = Executors.newFixedThreadPool(10);
        CountDownLatch latch = new CountDownLatch(numberOfExcute);


        for (int i = 0; i < numberOfExcute; i++) {
            service.execute(() -> {
                try {
                    redisLearnMeCountService.increaseAndGetServiceCount();
                } catch (ObjectOptimisticLockingFailureException oe) {
                    System.out.println("oe = " + oe.toString());
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }
                latch.countDown();
            });
        }
        latch.await();

        //then
        assertEquals(300, redisLearnMeCountService.getCurrentServiceCount());
    }

    @Test
    public void increaseAndGetServiceCount_성공(){
        //given

        //when
        for(int i = 0; i < 10; i++){
            redisLearnMeCountService.increaseAndGetServiceCount();
        }

        //then
        assertEquals(10,redisLearnMeCountService.getCurrentServiceCount());
    }

    @Test
    public void getServiceCount_반복사용(){
        //given
        for(int i = 0; i < 10; i++){
            redisLearnMeCountService.increaseAndGetServiceCount();
        }

        //when
        assertEquals(10,redisLearnMeCountService.getCurrentServiceCount());
        assertEquals(10,redisLearnMeCountService.getCurrentServiceCount());
        assertEquals(10,redisLearnMeCountService.getCurrentServiceCount());
        assertEquals(10,redisLearnMeCountService.getCurrentServiceCount());
        assertEquals(10,redisLearnMeCountService.getCurrentServiceCount());

        //then
    }
}