package com.lemondouble.lemonToolbox.config;

import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

import java.util.Map;

public class RedisTestContainerInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        GenericContainer<?> container = new GenericContainer<>(DockerImageName.parse("redis:5.0.3-alpine"))
                .withExposedPorts(6379);

        container.start();
        String host = container.getHost();
        Integer port = container.getFirstMappedPort();

        System.out.println("RedisTestContainerInitializer host = " + host);
        System.out.println("RedisTestContainerInitializer port = " + port);


        Map<String, String> properties = Map.of(
                "spring.redis.host", host,
                "spring.redis.port", port.toString()
        );

        TestPropertyValues.of(properties).applyTo(applicationContext.getEnvironment());

    }
}
