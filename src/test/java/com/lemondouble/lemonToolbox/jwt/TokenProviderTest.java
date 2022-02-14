package com.lemondouble.lemonToolbox.jwt;

import com.lemondouble.lemonToolbox.api.repository.entity.ServiceUser;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.Authentication;

@SpringBootTest
class TokenProviderTest {

    @Autowired
    TokenProvider tokenProvider;

    @Test
    public void createToken() throws Exception {
        //given
        ServiceUser createdUser = new ServiceUser();
        createdUser.setId(1L);

        //when
        String token = tokenProvider.createToken(createdUser);

        //then
        System.out.println("token = " + token);
    }
}