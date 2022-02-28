package com.lemondouble.lemonToolbox.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class EnvironmentAwareNotificator implements EnvironmentAware {

    private static Environment env = null;
    @Override
    public void setEnvironment(Environment environment) {
        env = environment;
        String[] activeProfiles = env.getActiveProfiles();
        log.info("--------------------------------------------");
        log.info("EnvironmentAwareNotificator : notice Env Settings");
        for (String activeProfile : activeProfiles) {
            log.info("EnvironmentAwareNotificator : Current env: {}", activeProfile);
        }
        log.info("--------------------------------------------");

        String[] defaultProfiles = env.getDefaultProfiles();
        log.info("--------------------------------------------");
        for (String defaultProfile : defaultProfiles) {
            log.info("EnvironmentAwareNotificator : Default env: {}", defaultProfile);
        }
        log.info("--------------------------------------------");
    }
}
