package com.lemondouble.lemonToolbox.api.controller;

import com.lemondouble.lemonToolbox.api.service.TwitterOauthService;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
class OAuthControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    TwitterOauthService twitterOauthService;

}