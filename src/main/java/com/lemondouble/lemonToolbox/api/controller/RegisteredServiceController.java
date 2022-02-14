package com.lemondouble.lemonToolbox.api.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.lemondouble.lemonToolbox.api.dto.OAuth.TokenDto;
import com.lemondouble.lemonToolbox.api.repository.entity.OAuthToken;
import com.lemondouble.lemonToolbox.api.repository.entity.ServiceType;
import com.lemondouble.lemonToolbox.api.service.RegisteredServiceService;
import com.lemondouble.lemonToolbox.api.service.SqsMessageService;
import com.lemondouble.lemonToolbox.api.service.TwitterUserService;
import com.lemondouble.lemonToolbox.api.util.SecurityUtil;
import com.lemondouble.lemonToolbox.jwt.TokenProvider;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/service")
@Tag(name = "registered-service-controller",description = "LearnMe, 트윗청소기 등 서비스 관련")
public class RegisteredServiceController {

    private final RegisteredServiceService registeredServiceService;
    private final TwitterUserService twitterUserService;
    private final SqsMessageService sqsMessageService;
    private final TokenProvider tokenProvider;


    public RegisteredServiceController(RegisteredServiceService registeredServiceService, TwitterUserService twitterUserService, SqsMessageService sqsMessageService, TokenProvider tokenProvider) {
        this.registeredServiceService = registeredServiceService;
        this.twitterUserService = twitterUserService;
        this.sqsMessageService = sqsMessageService;
        this.tokenProvider = tokenProvider;
    }

    /**
     * 해당 유저의 JWT 바탕으로 Learn Me 서비스에 등록시킨다. <br>
     * 이후, SQS 에 서비스 요청 날려 해당 유저의 ML 데이터 준비한다. <br>
     * 아래 프로세스가 순차적으로 실행된다. <br>
     * Access Token, Secret 과 User Id 실어서 보내면 AWS Lambda 에 의해 다음 처리가 순차적으로 실행된다. <br>
     * 1. 해당 유저의 트윗을 3200개 (Limit) 까지 가져온다. <br>
     * 2. 나한테 온 멘션을 Question, 내가 보낸 답멘을 Answer 로 정리한다. <br>
     * 3. 다음 Pipeline 에 의해 중복 Tweet 은 제거된다. <br>
     * 4. Bert Model 을 통해 해당 트윗들을 Embedding 해 S3에 .pickle 로 저장한다. <br>
     * 5. TODO : 작업 완료 알람을 보내고 트위터에 알람을 보낸다. <br>
     */
    @ApiOperation(value = "Learn Me 서비스 등록")
    @ResponseStatus(value = HttpStatus.CREATED)
    @PostMapping("service/learn_me")
    public ResponseEntity<Void> registerLearnMe() throws JsonProcessingException {
        Long currentId = getUserId();

        // 현재 유저를 learn Me 서비스에 가입시킨다
        registeredServiceService.joinLearnMe(currentId);

        // 현재 유저의 oAuthToken 으로 SQS Queue 에 서비스 요청 날린다.
        OAuthToken oAuthToken = twitterUserService.getOAuthTokenByUserId(currentId);
        sqsMessageService.sendToRequestTweetQueue(oAuthToken);

        return new ResponseEntity<>(null, HttpStatus.CREATED);
    }


    /**
     * TODO
     * 이후 AWS Lambda 에 챗봇 요청 보낼때, 권한 요청을 위해 이 서버에서 JWT 발급 후, Lambda에서 JWT 인증을 할 예정. <br>
     * 따라서 현재 유저 ID 기반으로 해당 유저의 Learn Me 서비스에 접근하는 것이 허용되는지 확인 후, 해당 USER ID의 TOKEN 발급 <br>
     */
    @ApiOperation(value = "Learn me AWS Lambda 접근 위한 JWT 토큰 발급",
            notes = "각 유저의 public 설정 여부 등을 고려하여 30분짜리 인증 Token을 발급해 줌. " +
                    "이후 이 JWT Token을 Lambda에 제출하면 챗봇 사용 가능")
    @PostMapping("service/learn_me/token")
    @ResponseStatus(value = HttpStatus.CREATED)
    public ResponseEntity<TokenDto> getLearnMeAccessToken(@RequestParam("access_id") Long accessId){

        // 서비스가 준비되었는지 확인
        boolean isReady = registeredServiceService.checkServiceIsReady(accessId, ServiceType.LEARNME);
        if(!isReady){
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Service is not ready! please try later");
        }

        //만약 자신의 Token 요청한다면, public 확인 없이 바로 Token 발급해줌
        Long currentUserId = getUserId();
        if(currentUserId.equals(accessId)){
            String jwt = tokenProvider.createLearnMeAccessToken(accessId);
            return new ResponseEntity<>(new TokenDto(jwt), HttpStatus.OK);
        }

        // 만약 자신의 Token 아니라면, public인 경우에만 Token 발급해줌
        boolean isPublic = registeredServiceService.checkServiceIsPublic(accessId, ServiceType.LEARNME);
        if(!isPublic){
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "You don't have permission to access that user's data"
            );
        }

        String jwt = tokenProvider.createLearnMeAccessToken(accessId);
        return new ResponseEntity<>(new TokenDto(jwt), HttpStatus.CREATED);
    }

    private Long getUserId() {
        return SecurityUtil.getCurrentUserId().orElseThrow(()-> new RuntimeException("인증 Error, 현재 유저를 찾을 수 없습니다!"));
    }
}
