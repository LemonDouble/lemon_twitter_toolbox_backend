package com.lemondouble.lemonToolbox.api.sqsListener;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.lemondouble.lemonToolbox.api.dto.sqs.ServiceReadyResponseDto;
import com.lemondouble.lemonToolbox.api.repository.OAuthTokenRepository;
import com.lemondouble.lemonToolbox.api.repository.RegisteredServiceRepository;
import com.lemondouble.lemonToolbox.api.repository.entity.OAuthToken;
import com.lemondouble.lemonToolbox.api.repository.entity.RegisteredService;
import com.lemondouble.lemonToolbox.api.repository.entity.ServiceUser;
import com.lemondouble.lemonToolbox.api.service.SqsMessageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.aws.messaging.listener.Acknowledgment;
import org.springframework.cloud.aws.messaging.listener.SqsMessageDeletionPolicy;
import org.springframework.cloud.aws.messaging.listener.annotation.SqsListener;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class ReadyServiceListener {

    private final OAuthTokenRepository oAuthTokenRepository;
    private final RegisteredServiceRepository registeredServiceRepository;
    private final SqsMessageService sqsMessageService;


    public ReadyServiceListener(OAuthTokenRepository oAuthTokenRepository, RegisteredServiceRepository registeredServiceRepository , SqsMessageService sqsMessageService) {
        this.oAuthTokenRepository = oAuthTokenRepository;
        this.registeredServiceRepository = registeredServiceRepository;
        this.sqsMessageService = sqsMessageService;
    }

    /**
     * SQS에서 서비스 처리 완료되었다는 메세지가 들어오면, DB에 해당 서비스를 isReady = true로 변경한다.
     */
    @SqsListener(value = "ServiceReadyQueue", deletionPolicy = SqsMessageDeletionPolicy.NEVER)
    @Transactional
    public void listen(String payload, @Headers Map<String, String> headers, Acknowledgment ack) throws JsonProcessingException {
        try{
            // 받아온 JSON String을 Dto로 Mapping
            ObjectMapper mapper = new ObjectMapper().setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
            ServiceReadyResponseDto serviceReadyResponseDto
                    = mapper.readValue(payload, ServiceReadyResponseDto.class);
            log.debug("ReadyServiceListener : JSON Convert Complete");
            log.debug("ReadyServiceListener data : {}", serviceReadyResponseDto.toString());
            List<OAuthToken> oauthToken = oAuthTokenRepository.findByOauthTypeAndOauthUserId(
                    serviceReadyResponseDto.getOAuthType(),
                    serviceReadyResponseDto.getOAuthUserId()
            );
            ServiceUser serviceUser = oauthToken.get(0).getServiceUser();
            log.debug("ReadyServiceListener : Get Service User Complete");

            RegisteredService registeredService = registeredServiceRepository.findByServiceUserAndServiceType(
                    serviceUser,
                    serviceReadyResponseDto.getServiceName()
            ).get(0);

            registeredService.setReady(true);
            log.debug("ReadyServiceListener : set registeredService Ready = true Complete");

            sqsMessageService.sendToTweetNotificationQueue(oauthToken.get(0));
            log.debug("ReadyServiceListener : sendToTweetNotificationQueue = Complete");

            ack.acknowledge();
        }catch(Exception e){
            log.error("ReadyServiceListener : Processing Failed {}" , e.toString());
        }
    }
}
