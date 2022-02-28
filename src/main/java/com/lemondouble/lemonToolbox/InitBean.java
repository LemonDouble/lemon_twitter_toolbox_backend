package com.lemondouble.lemonToolbox;

import com.lemondouble.lemonToolbox.api.repository.ServiceCountRepository;
import com.lemondouble.lemonToolbox.api.repository.entity.ServiceCount;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component @Profile({"dev","test"})
public class InitBean implements InitializingBean {

    private final ServiceCountRepository serviceCountRepository;

    public InitBean(ServiceCountRepository serviceCountRepository) {
        this.serviceCountRepository = serviceCountRepository;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        ServiceCount learnme = new ServiceCount();
        learnme.setServiceName("LEARNME");
        learnme.setCount(0L);
        serviceCountRepository.save(learnme);
    }
}
