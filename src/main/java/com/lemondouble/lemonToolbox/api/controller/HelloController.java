package com.lemondouble.lemonToolbox.api.controller;

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
    @GetMapping("/hello")
    public ResponseEntity<String> hello(){
        Optional<String> currentUsername = SecurityUtil.getCurrentUsername();
        System.out.println("currentUsername.get() = " + currentUsername.get());
        return ResponseEntity.ok("hello");
    }

}
