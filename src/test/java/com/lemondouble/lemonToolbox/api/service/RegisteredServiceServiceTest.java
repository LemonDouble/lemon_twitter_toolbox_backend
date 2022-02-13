package com.lemondouble.lemonToolbox.api.service;

import com.lemondouble.lemonToolbox.api.repository.entity.ServiceUser;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class RegisteredServiceServiceTest {

    @Autowired
    RegisteredServiceService registeredServiceService;



    @Test
    public void checkDuplicateJoinLearnme() throws Exception {
        //given
        ServiceUser serviceUser = ServiceUser.builder().

        //when

        //then   
    }
}