package com.lemondouble.lemonToolbox.api.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.lemondouble.lemonToolbox.api.repository.entity.OAuthToken;
import com.lemondouble.lemonToolbox.api.service.RegisteredServiceService;
import com.lemondouble.lemonToolbox.api.service.SqsMessageService;
import com.lemondouble.lemonToolbox.api.service.TwitterUserService;
import com.lemondouble.lemonToolbox.api.util.SecurityUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/service")
public class RegisteredServiceController {

    private final RegisteredServiceService registeredServiceService;
    private final TwitterUserService twitterUserService;
    private final SqsMessageService sqsMessageService;

    public RegisteredServiceController(RegisteredServiceService registeredServiceService, TwitterUserService twitterUserService, SqsMessageService sqsMessageService) {
        this.registeredServiceService = registeredServiceService;
        this.twitterUserService = twitterUserService;
        this.sqsMessageService = sqsMessageService;
    }

    // TODO : 서비스 가입 두번 됨 수정해야됨!
    @PostMapping("service/learn_me")
    public ResponseEntity<Void> registerLearnMe() throws JsonProcessingException {
        Long currentId = getUserId();

        // 현재 유저를 learn Me 서비스에 가입시킨다
        registeredServiceService.joinLearnMe(currentId);

        // 현재 유저의 oAuthToken으로 sqs Queue에 서비스 요청 날린다.
        OAuthToken oAuthToken = twitterUserService.getOAuthTokenByUserId(currentId);
        sqsMessageService.sendToRequestTweetQueue(oAuthToken);

        return new ResponseEntity<>(null, HttpStatus.CREATED);
    }

    private Long getUserId() {
        return SecurityUtil.getCurrentUserId().orElseThrow(()-> new RuntimeException("인증 Error, 현재 유저를 찾을 수 없습니다!"));
    }
}
