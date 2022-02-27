package com.lemondouble.lemonToolbox.api.schedular;


import com.lemondouble.lemonToolbox.api.repository.ServiceCountRepository;

import com.lemondouble.lemonToolbox.api.repository.entity.ServiceCount;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
public class CounterResetter {

    private final ServiceCountRepository serviceCountRepository;

    public CounterResetter(ServiceCountRepository serviceCountRepository) {
        this.serviceCountRepository = serviceCountRepository;
    }

    @Transactional
    @Scheduled(cron = "0 0 6 * * *")
    public void counterReset(){
        log.info("counterReset Called");

        ServiceCount learnmeCount =
                serviceCountRepository.findById("LEARNME").orElseThrow(() -> new RuntimeException("LEARNME Counter가 없습니다!"));

        learnmeCount.setCount(0L);
    }
}
