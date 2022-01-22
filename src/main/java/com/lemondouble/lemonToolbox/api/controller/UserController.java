package com.lemondouble.lemonToolbox.api.controller;

import com.lemondouble.lemonToolbox.api.dto.UserDto;
import com.lemondouble.lemonToolbox.api.service.UserService;
import com.lemondouble.lemonToolbox.api.repository.entity.User;
import com.lemondouble.lemonToolbox.jwt.TokenProvider;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /*
    @PostMapping("/signup")
    public ResponseEntity<User> signup(
            @Valid @RequestBody UserDto userDto
    ){
        return ResponseEntity.ok(userService.signup(userDto));
    }

     */
}
