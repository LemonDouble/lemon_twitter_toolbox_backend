package com.lemondouble.lemonToolbox.config;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSAsyncClientBuilder;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import org.junit.Rule;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration
public class LocalStackSqsConfig {
    DockerImageName localstackImage = DockerImageName.parse("localstack/localstack:latest");

    @Bean(initMethod = "start", destroyMethod = "stop")
    public LocalStackContainer localStackContainer() {
        return new LocalStackContainer(localstackImage)
                .withServices(LocalStackContainer.Service.SQS);
    }

    @Bean
    public AmazonSQS amazonSQS(LocalStackContainer localStackContainer){
        return AmazonSQSAsyncClientBuilder.standard()
                .withEndpointConfiguration(localStackContainer.getEndpointConfiguration(LocalStackContainer.Service.SQS))
                .withCredentials(localStackContainer.getDefaultCredentialsProvider())
                .build();
    }
}
