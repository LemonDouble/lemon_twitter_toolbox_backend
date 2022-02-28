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
import com.lemondouble.lemonToolbox.api.repository.ServiceCountRepository;
import com.lemondouble.lemonToolbox.api.repository.entity.OAuthToken;
import com.lemondouble.lemonToolbox.api.repository.entity.RegisteredService;
import com.lemondouble.lemonToolbox.api.repository.entity.ServiceCount;
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
    private final ServiceCountRepository serviceCountRepository;
    private final OAuthTokenRepository oAuthTokenRepository;
    private final RegisteredServiceRepository registeredServiceRepository;

    private Long count = 0L;

    @Value("${service-limit.learnme}")
    private Long LEARNME_LIMIT;

    public SqsMessageService(AmazonSQS amazonSQS,
                             ServiceCountRepository serviceCountRepository,
                             OAuthTokenRepository oAuthTokenRepository,
                             RegisteredServiceRepository registeredServiceRepository) {
        this.queueMessagingTemplate = new QueueMessagingTemplate((AmazonSQSAsync) amazonSQS);
        this.serviceCountRepository = serviceCountRepository;
        this.oAuthTokenRepository = oAuthTokenRepository;
        this.registeredServiceRepository = registeredServiceRepository;
        // SQS 메세지 보낼때는 Snake Case로 보낸다.
        this.objectMapper = new ObjectMapper().setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
    }

    /**
     * Learn Me 서비스용 메소드 <br>
     * TweetGetRequestQueue 로 요청을 보낸다. <br>
     * 단, 오늘 사용자가 LEARNME_LIMIT 이상이라면 요청 보내지 않는다.
     * Access Token, Secret 과 User Id 실어서 보내면 AWS Lambda 에 의해 다음 처리가 순차적으로 실행된다. <br>
     * 1. 해당 유저의 트윗을 3200개 (Limit) 까지 가져온다. <br>
     * 2. 나한테 온 멘션을 Question, 내가 보낸 답멘을 Answer 로 정리한다. <br>
     * 3. 다음 Pipeline 에 의해 중복 Tweet 은 제거된다. <br>
     * 4. Bert Model 을 통해 해당 트윗들을 Embedding 해 S3에 .pickle 로 저장한다. <br>
     * 5. 작업 완료 알람을 보내고 트위터에 알람을 보낸다., 완료되었음을 Spring Server에 알리고 isReady를 True로 바꾼다. <br>
     */

    // 임시방편 : Isolation Level을 SERIALIZABLE로. Race Condition 문제 해결
    // TODO : Redis 등으로 대체 필요
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public LearnMeRegisterResponseDto sendToRequestTweetQueue(OAuthToken RequestUserOAuthToken) throws JsonProcessingException {
        queueUserRequestDto requestDto = queueUserRequestDto.builder()
                .finished("spring_request_fetch_tweet")
                .userId(RequestUserOAuthToken.getOauthUserId().toString())
                .AccessToken(RequestUserOAuthToken.getAccessToken())
                .AccessSecret(RequestUserOAuthToken.getAccessTokenSecret())
                .build();

        Message<String> message = dtoToMessage(requestDto);

        // serviceCountRepository 의 1번 entity : Learn-Me count
        ServiceCount learnmeCount = serviceCountRepository.findById("LEARNME")
                .orElseThrow(() -> {
                    throw new RuntimeException("LEARNME Count가 없습니다!");
                });

        if(learnmeCount.getCount() > LEARNME_LIMIT){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "오늘 사용가능한 사람 수를 넘었습니다!");
        }
        learnmeCount.setCount(learnmeCount.getCount()+1);
        queueMessagingTemplate.send("TweetGetRequestQueue", message);


        return LearnMeRegisterResponseDto.builder()
                .registerCount(learnmeCount.getCount()).registerLimit(LEARNME_LIMIT).build();
    }

    public void sendToTweetNotificationQueue(OAuthToken RequestUserOAuthToken) throws JsonProcessingException {
        queueNotificationRequestDto requestDto = queueNotificationRequestDto.builder()
                .finished("spring_success_marking")
                .userId(RequestUserOAuthToken.getOauthUserId().toString())
                .AccessToken(RequestUserOAuthToken.getAccessToken())
                .AccessSecret(RequestUserOAuthToken.getAccessTokenSecret())
                .message("[Twitter Toolbox] 훈련이 완료되었어요! 이제 나를 따라하는 봇을 사용해 볼 수 있어요! https://toolbox.lemondouble.com/learn-me #LemonTwitterToolbox")
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
            count++;
            log.debug("processing Count : {}", count);
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


    // 문자 그대로 dto 를 message 로..
    private Message<String> dtoToMessage(Object object) throws JsonProcessingException {
        String payload = dtoToString(object);
        return MessageBuilder.withPayload(payload).build();
    }

    // 문자 그대로 dto 를 String 으로..
    private String dtoToString(Object dto) throws JsonProcessingException {
        return objectMapper.writeValueAsString(dto);
    }
}
