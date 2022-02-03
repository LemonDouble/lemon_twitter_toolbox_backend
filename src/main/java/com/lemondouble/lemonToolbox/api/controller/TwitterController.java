package com.lemondouble.lemonToolbox.api.controller;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.lemondouble.lemonToolbox.api.dto.Twitter.TwitterProfileDto;
import com.lemondouble.lemonToolbox.api.dto.sqs.queueUserRequestDto;
import com.lemondouble.lemonToolbox.api.repository.entity.OAuthToken;
import com.lemondouble.lemonToolbox.api.service.SqsMessageService;
import com.lemondouble.lemonToolbox.api.service.TwitterUserService;
import com.lemondouble.lemonToolbox.api.util.SecurityUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import twitter4j.TwitterException;
import twitter4j.User;

@RestController
@RequestMapping("/api/twitter")
public class TwitterController {
    private TwitterUserService twitterUserService;
    private final SqsMessageService sqsMessageService;

    public TwitterController(TwitterUserService twitterUserService, SqsMessageService sqsMessageService) {
        this.twitterUserService = twitterUserService;
        this.sqsMessageService = sqsMessageService;
    }

    @GetMapping("/user-profile")
    public TwitterProfileDto getProfileURL() throws TwitterException {

        Long currentUserId = getUserId();
        User currentUser = twitterUserService.getTwitterUserByUserId(currentUserId);

        return TwitterProfileDto.builder()
                .screenName(currentUser.getScreenName())
                .screenNickname(currentUser.getName())
                .userBio(currentUser.getDescription())
                .profileImageURL(currentUser.get400x400ProfileImageURLHttps())
                .bannerImageURL(currentUser.getProfileBanner1500x500URL())
                .build();
    }

    // TODO : 어떤 서비스 가입중인지, Service Entity 추가하자
    @PostMapping("service/learn_me")
    public ResponseEntity<Void> registerLearnMe() throws JsonProcessingException {
        Long currentId = getUserId();

        OAuthToken oAuthToken = twitterUserService.getOAuthTokenByUserId(currentId);
        queueUserRequestDto requestDto = queueUserRequestDto.builder()
                .userId(oAuthToken.getOauthUserId())
                .AccessToken(oAuthToken.getAccessToken())
                .AccessSecret(oAuthToken.getAccessTokenSecret())
                .build();

        sqsMessageService.sendToRequestTweetQueue(requestDto);

        return new ResponseEntity<>(null, HttpStatus.CREATED);
    }

    private Long getUserId() {
        return SecurityUtil.getCurrentUserId().orElseThrow(()-> new RuntimeException("인증 Error, 현재 유저를 찾을 수 없습니다!"));
    }
}
