package com.lemondouble.lemonToolbox.jwt;

import com.lemondouble.lemonToolbox.api.repository.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.Authentication;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class TokenProviderTest {

    @Autowired
    TokenProvider tokenProvider;

    @Test
    public void createToken() throws Exception {
        //given
        User createdUser = new User();
        createdUser.setId(1L);

        //when
        String token = tokenProvider.createToken(createdUser);

        //then
        System.out.println("token = " + token);
    }

    @Test
    public void getAuthentication() throws Exception {
        //given
        String token = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJ1c2VyIiwidXNlcl9pZCI6MSwiZXhwIjoxNjQ1NDg3MjIwfQ.nr6xjPUjBeSly7CyZXDYYJ6RJ5llK8c8dmmrRKBy8uruvj-irI6baojAuM_LaNPnmcWb3A0jSFcexE7wmQJdqA";

        //when
        Authentication authentication = tokenProvider.getAuthentication(token);

        //then

        System.out.println("authentication = " + authentication);
    }
}