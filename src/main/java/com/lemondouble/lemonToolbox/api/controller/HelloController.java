package com.lemondouble.lemonToolbox.api.controller;

import com.lemondouble.lemonToolbox.api.dto.kafka.TestDto;
import com.lemondouble.lemonToolbox.api.service.KafkaService;
import com.lemondouble.lemonToolbox.api.util.SecurityUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import twitter4j.Twitter;

import java.util.Optional;

@RestController
@RequestMapping("/api")
public class HelloController {

    private final KafkaService kafkaService;

    public HelloController(KafkaService kafkaService) {
        this.kafkaService = kafkaService;
    }

    @GetMapping("/hello")
    public ResponseEntity<String> hello(){

        TestDto testDto = new TestDto("name" , "message" , 10);
        kafkaService.sendMessage(testDto);

        return ResponseEntity.ok("hello");
    }

}
