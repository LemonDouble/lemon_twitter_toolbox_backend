package com.lemondouble.lemonToolbox.api.service;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSAsync;
import com.lemondouble.lemonToolbox.api.dto.kafka.TestDto;
import org.springframework.cloud.aws.messaging.core.QueueMessagingTemplate;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

@Service
public class SqsMessageService {

    private final QueueMessagingTemplate queueMessagingTemplate;

    public SqsMessageService(AmazonSQS amazonSQS) {
        this.queueMessagingTemplate = new QueueMessagingTemplate((AmazonSQSAsync) amazonSQS);
    }

    public void sendMessage(){
        TestDto testUserData = TestDto.builder().AccessKey("AAA").AccessSecret("BBB").userId(10L).build();

        Message<TestDto> newMessage = MessageBuilder.withPayload(testUserData).build();
        queueMessagingTemplate.send("Test", newMessage);
    }
}
