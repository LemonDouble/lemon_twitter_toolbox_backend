package com.lemondouble.lemonToolbox.api.service;

import com.amazonaws.services.sqs.AmazonSQS;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.lemondouble.lemonToolbox.api.repository.entity.OAuthToken;
import com.lemondouble.lemonToolbox.config.LocalStackSqsConfig;
import com.lemondouble.lemonToolbox.config.RedisTestContainerInitializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = LocalStackSqsConfig.class)
@ContextConfiguration(initializers = {RedisTestContainerInitializer.class})
class SqsMessageServiceTest {

    @Autowired
    AmazonSQS amazonSQS;

    @Autowired
    SqsMessageService sqsMessageService;

    @Autowired
    RedisLearnMeCountService redisLearnMeCountService;

    @BeforeEach
    @Transactional
    void init(){
        amazonSQS.createQueue("TweetGetRequestQueue");
        redisLearnMeCountService.setServiceCountToZero();
    }


    @Test
    @Transactional
    public void sendMessage_성공() throws JsonProcessingException {

        //given

        OAuthToken token = OAuthToken.builder()
                .accessToken("TOKEN")
                .accessTokenSecret("SECRET")
                .oauthUserId(77777L).build();
        //when

        //then
        sqsMessageService.sendToRequestTweetQueue(token);
    }

    @Test
    @Transactional
    public void sendMessage_400번이후_실패() throws JsonProcessingException, InterruptedException {
        //given
        redisLearnMeCountService.setServiceCountToZero();

        //when
        OAuthToken token = OAuthToken.builder()
                .accessToken("TOKEN")
                .accessTokenSecret("SECRET")
                .oauthUserId(77777L).build();

        int numberOfExcute = 400;
        ExecutorService service = Executors.newFixedThreadPool(10);
        CountDownLatch latch = new CountDownLatch(numberOfExcute);


        for (int i = 0; i < numberOfExcute; i++) {
            service.execute(() -> {
                try {
                    sqsMessageService.sendToRequestTweetQueue(token);
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
        assertThrows(ResponseStatusException.class, ()->{
            sqsMessageService.sendToRequestTweetQueue(token);
        });
        assertThrows(ResponseStatusException.class, ()->{
            sqsMessageService.sendToRequestTweetQueue(token);
        });

        assertEquals(numberOfExcute+2, redisLearnMeCountService.getCurrentServiceCount());
;
    }
}