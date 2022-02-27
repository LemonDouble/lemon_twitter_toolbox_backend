package com.lemondouble.lemonToolbox.api.service;

import com.amazonaws.services.sqs.AmazonSQS;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.lemondouble.lemonToolbox.api.repository.ServiceCountRepository;
import com.lemondouble.lemonToolbox.api.repository.entity.OAuthToken;
import com.lemondouble.lemonToolbox.api.repository.entity.ServiceCount;
import com.lemondouble.lemonToolbox.config.LocalStackSqsConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = LocalStackSqsConfig.class)
class SqsMessageServiceTest {

    @Autowired
    AmazonSQS amazonSQS;

    @Autowired
    SqsMessageService sqsMessageService;

    @Autowired
    ServiceCountRepository serviceCountRepository;

    @BeforeEach
    @Transactional
    void init(){
        amazonSQS.createQueue("TweetGetRequestQueue");
        ServiceCount learnme = new ServiceCount();
        learnme.setServiceName("LEARNME");
        learnme.setCount(0L);
        serviceCountRepository.save(learnme);
    }


    @Test
    @Transactional
    public void sendMessage_성공() throws JsonProcessingException {

        //given

        OAuthToken token = OAuthToken.builder()
                .accessToken("TOKEN")
                .accessTokenSecret("SECRET")
                .oauthUserId(77777L).build();
        sqsMessageService.sendToRequestTweetQueue(token);

        //when

        //then
    }

    @Test
    @Transactional
    public void sendMessage_300번이후_실패() throws JsonProcessingException {
        //given

        //when
        OAuthToken token = OAuthToken.builder()
                .accessToken("TOKEN")
                .accessTokenSecret("SECRET")
                .oauthUserId(77777L).build();

        for(int i =0; i <= 300; i++){
            // request 300번 보냄
            sqsMessageService.sendToRequestTweetQueue(token);
        }

        //then
        assertThrows(ResponseStatusException.class, ()->{
            sqsMessageService.sendToRequestTweetQueue(token);
        });
    }
}