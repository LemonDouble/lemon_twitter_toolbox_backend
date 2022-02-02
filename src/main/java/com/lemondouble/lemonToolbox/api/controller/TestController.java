package com.lemondouble.lemonToolbox.api.controller;

import com.lemondouble.lemonToolbox.api.dto.kafka.TestDto;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import twitter4j.TwitterException;

@RestController
@RequestMapping("/api")
public class TestController {

    @GetMapping("/test")
    public void hello() throws TwitterException {
    }

}
