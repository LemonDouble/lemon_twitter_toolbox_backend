package com.lemondouble.lemonToolbox.api.service;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSAsync;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lemondouble.lemonToolbox.api.dto.kafka.TestDto;
import org.springframework.cloud.aws.messaging.core.QueueMessagingTemplate;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

@Service
public class SqsMessageService {

    private final QueueMessagingTemplate queueMessagingTemplate;
    private final ObjectMapper objectMapper;

    public SqsMessageService(AmazonSQS amazonSQS) {
        this.queueMessagingTemplate = new QueueMessagingTemplate((AmazonSQSAsync) amazonSQS);
        this.objectMapper = new ObjectMapper();
    }

    public void sendMessage() throws JsonProcessingException {
        TestDto testUserData = TestDto.builder().AccessKey("AAA").AccessSecret("BBB").userId(10L).build();
        String payload = dtoToString(testUserData);
        Message<String> newMessage = MessageBuilder.withPayload(payload).build();
        queueMessagingTemplate.send("Test", newMessage);
    }

    private String dtoToString(Object dto) throws JsonProcessingException {
        return objectMapper.writeValueAsString(dto);
    }
}
