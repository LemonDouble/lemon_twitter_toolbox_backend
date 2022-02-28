package com.lemondouble.lemonToolbox.api.service;

import com.lemondouble.lemonToolbox.api.dto.RegisteredService.RegisteredServiceModifyDto;
import com.lemondouble.lemonToolbox.api.repository.RegisteredServiceRepository;
import com.lemondouble.lemonToolbox.api.repository.ServiceUserRepository;
import com.lemondouble.lemonToolbox.api.repository.entity.RegisteredService;
import com.lemondouble.lemonToolbox.api.repository.entity.ServiceType;
import com.lemondouble.lemonToolbox.api.repository.entity.ServiceUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
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
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public void joinLearnMe(Long userId){
        ServiceUser serviceUser = getServiceUserByUserId(userId);

        List<RegisteredService> findRegisteredData =
                registeredServiceRepository.findByServiceUserAndServiceType(serviceUser, ServiceType.LEARNME);

        // 회원가입 되어 있지 않은 경우에만 회원가입
        if(findRegisteredData.isEmpty()){
            RegisteredService registeredService = RegisteredService.builder()
                    .serviceType(ServiceType.LEARNME)
                    .serviceUser(serviceUser)
                    .build();

            registeredServiceRepository.save(registeredService);
        }
    }

    /**
     * Learn Me 서비스 탈퇴. <br>
     * 만약 이미 탈퇴되어 있다면 무시한다.
     * 탈퇴되어 있지 않다면, 탈퇴시킨다.
     */
    @Transactional
    public void unJoinLearnMe(Long userId){
        ServiceUser serviceUser = getServiceUserByUserId(userId);

        List<RegisteredService> findRegisteredData =
                registeredServiceRepository.findByServiceUserAndServiceType(serviceUser, ServiceType.LEARNME);

        if(findRegisteredData.size() > 1){
            throw new RuntimeException("unJoinLearnMe Error! 한 유저가 Learn Me 서비스에 중복가입되어 있습니다!");
       }

        // 탈퇴되어 있지 않은 경우, 탈퇴시킨다.
        if(findRegisteredData.size() == 1){
            RegisteredService registeredService = findRegisteredData.get(0);
            registeredServiceRepository.delete(registeredService);
        }
    }

    @Transactional
    public void setNextUseTime(Long userId, ServiceType serviceType, LocalDateTime nextUseTime){
        ServiceUser serviceUser = getServiceUserByUserId(userId);

        RegisteredService registeredService = getOneServiceUserAndServiceType(serviceUser, serviceType);
        registeredService.setCanUseTime(nextUseTime);
    }

    /**
     * 유저 Id와 Service Type (Learn Me.. 트윗청소기..) 받아 해당 서비스가 준비되어 있는지 리턴해 준다. <br>
     * 주로 사전작업 필요한 Learn me 서비스 등에 쓰일 예정
     */
    @Transactional(readOnly = true)
    public boolean checkServiceIsReady(Long userId, ServiceType serviceType){
        ServiceUser serviceUser = getServiceUserByUserId(userId);

        RegisteredService registeredService = getOneServiceUserAndServiceType(serviceUser, serviceType);

        return registeredService.isReady();
    }

    /**
     * 유저 Id와 서비스 타입(Learnme..) 등 받아 해당 서비스가 public인지 확인한다.
     */
    @Transactional(readOnly = true)
    public boolean checkServiceIsPublic(Long userId, ServiceType serviceType){
        ServiceUser serviceUser = getServiceUserByUserId(userId);

        RegisteredService registeredService = getOneServiceUserAndServiceType(serviceUser, serviceType);

        return registeredService.isPublic();
    }

    /**
     * 유저 Id 받아 해당 유저가 가입중인 서비스를 리턴한다.
     */
    @Transactional(readOnly = true)
    public List<RegisteredService> getMyRegisteredServicesByUserId(Long userId){
        ServiceUser serviceUser = getServiceUserByUserId(userId);

        return registeredServiceRepository.findByServiceUser(serviceUser);
    }

    @Transactional(readOnly = true)
    public RegisteredService getOneRegisteredServicesByUserIdAndType(Long userId, ServiceType serviceType){
        ServiceUser serviceUser = getServiceUserByUserId(userId);

        return getOneServiceUserAndServiceType(serviceUser, serviceType);
    }



    @Transactional
    public RegisteredService modifyServiceInfoByUserIdAndServiceDto(Long userId, RegisteredServiceModifyDto registeredServiceModifyDto){
        ServiceUser serviceUser = getServiceUserByUserId(userId);

        RegisteredService findService =
                getOneServiceUserAndServiceType(serviceUser, registeredServiceModifyDto.getServiceType());

        findService.setPublic(registeredServiceModifyDto.isPublic());

        return findService;
    }

    /**
     * 해당 유저와 Service Type 을 받아 해당 서비스 Entity 를 리턴해 준다. <br>
     * 만약 찾는 서비스가 없거나, 결과값이 하나가 아니라면 Exception 발생시킨다.
     */
    private RegisteredService getOneServiceUserAndServiceType(ServiceUser serviceUser, ServiceType serviceType) {
        List<RegisteredService> findServiceList =
                registeredServiceRepository.findByServiceUserAndServiceType(serviceUser, serviceType);

        if(findServiceList.isEmpty()){
            throw new RuntimeException("찾는 서비스가 없습니다!");
        }

        if(findServiceList.size() != 1){
            throw new RuntimeException("findByServiceUserAndServiceType 에러! 한 유저가 한 서비스에 중복 가입 되어 있습니다!");
        }

        return findServiceList.get(0);
    }

    /**
     * User Id 받아 해당 User 객체 리턴해 준다.
     */

    private ServiceUser getServiceUserByUserId(Long userId) {
        return serviceUserRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("찾는 유저가 없습니다!"));
    }
}
