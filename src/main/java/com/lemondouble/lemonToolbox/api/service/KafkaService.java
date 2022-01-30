package com.lemondouble.lemonToolbox.api.service;

import com.lemondouble.lemonToolbox.api.dto.kafka.TestDto;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class KafkaService {
    private static final String TOPIC = "test";
    private final KafkaTemplate<String, TestDto> kafkaTemplate;


    public KafkaService(KafkaTemplate kafkaTemplate){
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendMessage(TestDto testDto){
        kafkaTemplate.send(TOPIC, testDto);
    }
}
