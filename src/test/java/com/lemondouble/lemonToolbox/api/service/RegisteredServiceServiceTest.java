package com.lemondouble.lemonToolbox.api.service;

import com.lemondouble.lemonToolbox.api.repository.RegisteredServiceRepository;
import com.lemondouble.lemonToolbox.api.repository.ServiceUserRepository;
import com.lemondouble.lemonToolbox.api.repository.entity.RegisteredService;
import com.lemondouble.lemonToolbox.api.repository.entity.ServiceType;
import com.lemondouble.lemonToolbox.api.repository.entity.ServiceUser;
import com.lemondouble.lemonToolbox.config.LocalStackSqsConfig;
import com.lemondouble.lemonToolbox.config.RedisTestContainerInitializer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = LocalStackSqsConfig.class)
@ContextConfiguration(initializers = {RedisTestContainerInitializer.class})
@Transactional
class RegisteredServiceServiceTest {

    @Autowired
    RegisteredServiceRepository registeredServiceRepository;

    @Autowired
    ServiceUserRepository serviceUserRepository;

    @Autowired
    RegisteredServiceService registeredServiceService;

    @Test
    public void joinLearnMe_성공() throws Exception {
        //given

        // 유저 1 회원가입
        ServiceUser serviceUser = ServiceUser.builder()
                .nickname("유저1")
                .build();
        ServiceUser savedUser = serviceUserRepository.save(serviceUser);
        //when

        // 서비스 가입
        registeredServiceService.joinLearnMe(savedUser.getId());

        //then
        RegisteredService findService = registeredServiceRepository.findByServiceUserAndServiceType(
                savedUser,
                ServiceType.LEARNME
        ).get(0);

        assertEquals(ServiceType.LEARNME, findService.getServiceType());
        assertEquals(savedUser, findService.getServiceUser());
    }

    @Test
    public void joinLearnMe_중복가입시_가입하나만돼야됨() throws Exception {
        //given

        // 유저 1 회원가입
        ServiceUser serviceUser = ServiceUser.builder()
                .nickname("유저1")
                .build();
        ServiceUser savedUser = serviceUserRepository.save(serviceUser);

        //when

        // 중복 회원가입!!
        registeredServiceService.joinLearnMe(savedUser.getId());
        registeredServiceService.joinLearnMe(savedUser.getId());
        registeredServiceService.joinLearnMe(savedUser.getId());
        registeredServiceService.joinLearnMe(savedUser.getId());

        //then

        List<RegisteredService> findList = registeredServiceRepository.findByServiceUserAndServiceType(
                savedUser,
                ServiceType.LEARNME
        );

        assertEquals(1, findList.size());
    }
}

