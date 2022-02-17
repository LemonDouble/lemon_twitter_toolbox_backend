package com.lemondouble.lemonToolbox.api.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.lemondouble.lemonToolbox.api.dto.sqs.queueUserRequestDto;
import com.lemondouble.lemonToolbox.api.service.SqsMessageService;
import io.swagger.annotations.ApiOperation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import twitter4j.TwitterException;


@RestController
@RequestMapping("/api")
@Tag(name = "test-controller",description = "개발/테스트용 API")
public class TestController {

    private final SqsMessageService sqsMessageService;

    public TestController(SqsMessageService sqsMessageService) {
        this.sqsMessageService = sqsMessageService;
    }

    @ApiOperation(value = "테스트용")
    @GetMapping("/test")
    public String hello() throws TwitterException, JsonProcessingException {
        return "안뇽안뇽";
    }

}
