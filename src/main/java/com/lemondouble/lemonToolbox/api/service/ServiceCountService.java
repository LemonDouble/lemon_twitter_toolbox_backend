package com.lemondouble.lemonToolbox.api.service;

import com.lemondouble.lemonToolbox.api.repository.ServiceCountRepository;
import com.lemondouble.lemonToolbox.api.repository.entity.ServiceCount;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ServiceCountService {

    private final ServiceCountRepository serviceCountRepository;

    @Value("${service-limit.learnme}")
    private Long LEARNME_LIMIT;

    public ServiceCountService(ServiceCountRepository serviceCountRepository) {
        this.serviceCountRepository = serviceCountRepository;
    }

    @Transactional(readOnly = true)
    public boolean canUseLearnMeService(){
        ServiceCount learnmeServiceCount = serviceCountRepository.findById("LEARNME").orElseThrow(() -> {
            throw new RuntimeException("LEARNME Count가 없습니다!");
        });

        return learnmeServiceCount.getCount() <= (LEARNME_LIMIT-1);
    }

}
