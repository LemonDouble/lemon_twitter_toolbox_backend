package com.lemondouble.lemonToolbox;

import com.amazonaws.services.sqs.AmazonSQSAsync;
import com.lemondouble.lemonToolbox.config.LocalStackSqsConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.aws.messaging.core.QueueMessagingTemplate;
import org.springframework.context.annotation.Bean;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = LocalStackSqsConfig.class)
class LemonToolboxApplicationTests {

	@Test
	void contextLoads() {
	}


}
