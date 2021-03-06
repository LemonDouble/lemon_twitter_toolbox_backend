package com.lemondouble.lemonToolbox.api.service;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSAsync;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.lemondouble.lemonToolbox.api.dto.RegisteredService.LearnMeRegisterResponseDto;
import com.lemondouble.lemonToolbox.api.dto.sqs.ServiceReadyResponseDto;
import com.lemondouble.lemonToolbox.api.dto.sqs.queueUserRequestDto;
import com.lemondouble.lemonToolbox.api.dto.sqs.queueNotificationRequestDto;
import com.lemondouble.lemonToolbox.api.repository.OAuthTokenRepository;
import com.lemondouble.lemonToolbox.api.repository.RegisteredServiceRepository;
import com.lemondouble.lemonToolbox.api.repository.entity.OAuthToken;
import com.lemondouble.lemonToolbox.api.repository.entity.RegisteredService;
import com.lemondouble.lemonToolbox.api.repository.entity.ServiceUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.aws.messaging.core.QueueMessagingTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@Slf4j
public class SqsMessageService {

    private final QueueMessagingTemplate queueMessagingTemplate;
    private final ObjectMapper objectMapper;
    private final RedisLearnMeCountService redisLearnMeCountService;
    private final OAuthTokenRepository oAuthTokenRepository;
    private final RegisteredServiceRepository registeredServiceRepository;

    @Value("${service-limit.learnme}")
    private Long LEARNME_LIMIT;

    public SqsMessageService(AmazonSQS amazonSQS,
                             RedisLearnMeCountService redisLearnMeCountService,
                             OAuthTokenRepository oAuthTokenRepository,
                             RegisteredServiceRepository registeredServiceRepository) {
        this.queueMessagingTemplate = new QueueMessagingTemplate((AmazonSQSAsync) amazonSQS);
        this.redisLearnMeCountService = redisLearnMeCountService;
        this.oAuthTokenRepository = oAuthTokenRepository;
        this.registeredServiceRepository = registeredServiceRepository;
        // SQS ????????? ???????????? Snake Case??? ?????????.
        this.objectMapper = new ObjectMapper().setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
    }

    /**
     * Learn Me ???????????? ????????? <br>
     * TweetGetRequestQueue ??? ????????? ?????????. <br>
     * ???, ?????? ???????????? LEARNME_LIMIT ??????????????? ?????? ????????? ?????????.
     * Access Token, Secret ??? User Id ????????? ????????? AWS Lambda ??? ?????? ?????? ????????? ??????????????? ????????????. <br>
     * 1. ?????? ????????? ????????? 3200??? (Limit) ?????? ????????????. <br>
     * 2. ????????? ??? ????????? Question, ?????? ?????? ????????? Answer ??? ????????????. <br>
     * 3. ?????? Pipeline ??? ?????? ?????? Tweet ??? ????????????. <br>
     * 4. Bert Model ??? ?????? ?????? ???????????? Embedding ??? S3??? .pickle ??? ????????????. <br>
     * 5. ?????? ?????? ????????? ????????? ???????????? ????????? ?????????., ?????????????????? Spring Server??? ????????? isReady??? True??? ?????????. <br>
     */

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public LearnMeRegisterResponseDto sendToRequestTweetQueue(OAuthToken RequestUserOAuthToken) throws JsonProcessingException {
        queueUserRequestDto requestDto = queueUserRequestDto.builder()
                .finished("spring_request_fetch_tweet")
                .userId(RequestUserOAuthToken.getOauthUserId().toString())
                .AccessToken(RequestUserOAuthToken.getAccessToken())
                .AccessSecret(RequestUserOAuthToken.getAccessTokenSecret())
                .build();

        Message<String> message = dtoToMessage(requestDto);

        Long increasedCount = redisLearnMeCountService.increaseAndGetServiceCount();

        if(increasedCount > LEARNME_LIMIT){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "?????? ??????????????? ?????? ?????? ???????????????!");
        }

        queueMessagingTemplate.send("TweetGetRequestQueue", message);

        return LearnMeRegisterResponseDto.builder()
                .registerCount(increasedCount).registerLimit(LEARNME_LIMIT).build();
    }

    public void sendToTweetNotificationQueue(OAuthToken RequestUserOAuthToken) throws JsonProcessingException {
        queueNotificationRequestDto requestDto = queueNotificationRequestDto.builder()
                .finished("spring_success_marking")
                .userId(RequestUserOAuthToken.getOauthUserId().toString())
                .AccessToken(RequestUserOAuthToken.getAccessToken())
                .AccessSecret(RequestUserOAuthToken.getAccessTokenSecret())
                .message("[Twitter Toolbox] ????????? ??????????????????! ?????? ?????? ???????????? ?????? ????????? ??? ??? ?????????! https://toolbox.lemondouble.com/learn-me #LemonTwitterToolbox")
                .build();

        Message<String> message = dtoToMessage(requestDto);
        queueMessagingTemplate.send("TweetNotificationQueue", message);
    }

    public void sendToLearnMeTrainDataDeleteRequestQueue(OAuthToken oAuthToken) throws JsonProcessingException {
        queueNotificationRequestDto requestDto = queueNotificationRequestDto.builder()
                .finished("spring_request_delete")
                .userId(oAuthToken.getOauthUserId().toString())
                .build();

        Message<String> message = dtoToMessage(requestDto);
        queueMessagingTemplate.send("LearnMeTrainDataDeleteRequestQueue", message);
    }

    @Transactional
    public void processingServiceReadyResponseDto(ServiceReadyResponseDto serviceReadyResponseDto){
        try{

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

            sendToTweetNotificationQueue(oauthToken.get(0));
            log.debug("ReadyServiceListener : sendToTweetNotificationQueue = Complete");
        }catch(Exception e){
            log.error("ReadyServiceListener : One Data Processing Failed {}" , e.toString());
        }
    }


    // ?????? ????????? dto ??? message ???..
    private Message<String> dtoToMessage(Object object) throws JsonProcessingException {
        String payload = dtoToString(object);
        return MessageBuilder.withPayload(payload).build();
    }

    // ?????? ????????? dto ??? String ??????..
    private String dtoToString(Object dto) throws JsonProcessingException {
        return objectMapper.writeValueAsString(dto);
    }
}
