package com.lemondouble.lemonToolbox.api.service;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSAsync;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.lemondouble.lemonToolbox.api.dto.sqs.queueUserRequestDto;
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
        this.objectMapper = new ObjectMapper().setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);
    }

    public void sendToRequestTweetQueue(queueUserRequestDto queueUserRequestDto) throws JsonProcessingException {
        Message<String> message = dtoToMessage(queueUserRequestDto);
        queueMessagingTemplate.send("TweetGetRequestQueue", message);
    }



    private Message<String> dtoToMessage(Object object) throws JsonProcessingException {
        String payload = dtoToString(object);
        return MessageBuilder.withPayload(payload).build();
    }

    private String dtoToString(Object dto) throws JsonProcessingException {
        return objectMapper.writeValueAsString(dto);
    }
}
