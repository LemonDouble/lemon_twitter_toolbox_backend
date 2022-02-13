package com.lemondouble.lemonToolbox.api.service;

import com.lemondouble.lemonToolbox.api.repository.RegisteredServiceRepository;
import com.lemondouble.lemonToolbox.api.repository.ServiceUserRepository;
import com.lemondouble.lemonToolbox.api.repository.entity.RegisteredService;
import com.lemondouble.lemonToolbox.api.repository.entity.ServiceType;
import com.lemondouble.lemonToolbox.api.repository.entity.ServiceUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class RegisteredServiceService {
    private final RegisteredServiceRepository registeredServiceRepository;
    private final ServiceUserRepository serviceUserRepository;

    public RegisteredServiceService(RegisteredServiceRepository registeredServiceRepository, ServiceUserRepository serviceUserRepository) {
        this.registeredServiceRepository = registeredServiceRepository;
        this.serviceUserRepository = serviceUserRepository;
    }

    /**
     * Learn Me 서비스 가입. <br>
     * 만약 이미 가입되어 있다면 가입된 채로 내버려 두고, <br>
     * 가입되어 있지 않다면, 새로 가입시킨다.
     */
    @Transactional
    public void joinLearnMe(Long userId){
        ServiceUser serviceUser = serviceUserRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("찾는 유저가 없습니다!"));

        List<RegisteredService> findRegisteredData =
                registeredServiceRepository.findByServiceUserAndServiceType(serviceUser, ServiceType.LEARNME);

        // 회원가입 되어 있지 않은 경우에만 회원가입
        if(findRegisteredData.isEmpty()){
            RegisteredService registeredService = RegisteredService.builder()
                    .serviceType(ServiceType.LEARNME)
                    .serviceUser(serviceUser).build();

            registeredServiceRepository.save(registeredService);
        }
    }
}
