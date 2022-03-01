package com.lemondouble.lemonToolbox;

import com.amazonaws.services.sqs.AmazonSQSAsync;
import com.lemondouble.lemonToolbox.config.LocalStackSqsConfig;
import com.lemondouble.lemonToolbox.config.RedisTestContainerInitializer;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.aws.messaging.core.QueueMessagingTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ContextConfiguration;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = LocalStackSqsConfig.class)
@ContextConfiguration(initializers = {RedisTestContainerInitializer.class})
class LemonToolboxApplicationTests {

	@Test
	void contextLoads() {
	}


}
