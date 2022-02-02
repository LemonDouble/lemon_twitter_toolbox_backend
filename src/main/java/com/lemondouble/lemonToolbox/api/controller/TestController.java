package com.lemondouble.lemonToolbox.api.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.lemondouble.lemonToolbox.api.dto.kafka.TestDto;
import com.lemondouble.lemonToolbox.api.service.SqsMessageService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import twitter4j.TwitterException;

@RestController
@RequestMapping("/api")
public class TestController {

    private final SqsMessageService sqsMessageService;

    public TestController(SqsMessageService sqsMessageService) {
        this.sqsMessageService = sqsMessageService;
    }

    @GetMapping("/test")
    public void hello() throws TwitterException, JsonProcessingException {
        sqsMessageService.sendMessage();
    }

}
