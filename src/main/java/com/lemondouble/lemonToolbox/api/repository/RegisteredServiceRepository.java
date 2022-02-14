package com.lemondouble.lemonToolbox.api.repository;

import com.lemondouble.lemonToolbox.api.repository.entity.RegisteredService;
import com.lemondouble.lemonToolbox.api.repository.entity.ServiceType;
import com.lemondouble.lemonToolbox.api.repository.entity.ServiceUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RegisteredServiceRepository extends JpaRepository<RegisteredService, Long> {
    List<RegisteredService> findByServiceUserAndServiceType(ServiceUser serviceUser, ServiceType serviceType);
    List<RegisteredService> findByServiceUser(ServiceUser serviceUser);
}
