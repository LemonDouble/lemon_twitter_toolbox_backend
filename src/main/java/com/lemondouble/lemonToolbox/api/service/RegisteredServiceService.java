package com.lemondouble.lemonToolbox.api.service;

import com.lemondouble.lemonToolbox.api.repository.RegisteredServiceRepository;
import com.lemondouble.lemonToolbox.api.repository.ServiceUserRepository;
import com.lemondouble.lemonToolbox.api.repository.entity.RegisteredService;
import com.lemondouble.lemonToolbox.api.repository.entity.ServiceType;
import com.lemondouble.lemonToolbox.api.repository.entity.ServiceUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RegisteredServiceService {
    private final RegisteredServiceRepository registeredServiceRepository;
    private final ServiceUserRepository serviceUserRepository;

    public RegisteredServiceService(RegisteredServiceRepository registeredServiceRepository, ServiceUserRepository serviceUserRepository) {
        this.registeredServiceRepository = registeredServiceRepository;
        this.serviceUserRepository = serviceUserRepository;
    }

    @Transactional
    public void joinLearnMe(Long userId){
        ServiceUser serviceUser = serviceUserRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("찾는 유저가 없습니다!"));

        RegisteredService registeredService = RegisteredService.builder()
                .serviceType(ServiceType.LEARNME)
                .serviceUser(serviceUser).build();

        registeredServiceRepository.save(registeredService);
    }
}
