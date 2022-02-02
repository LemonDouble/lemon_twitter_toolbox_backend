package com.lemondouble.lemonToolbox.api.controller;


import com.lemondouble.lemonToolbox.api.dto.Twitter.TwitterProfileDto;
import com.lemondouble.lemonToolbox.api.service.TwitterUserService;
import com.lemondouble.lemonToolbox.api.util.SecurityUtil;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import twitter4j.TwitterException;
import twitter4j.User;

import java.util.Optional;

@RestController
@RequestMapping("/api/twitter")
public class TwitterController {
    private TwitterUserService twitterUserService;

    public TwitterController(TwitterUserService twitterUserService) {
        this.twitterUserService = twitterUserService;
    }

    @GetMapping("/user-profile")
    public TwitterProfileDto getProfileURL() throws TwitterException {

        Long currentUserId = SecurityUtil.getCurrentUserId().orElseThrow(()-> new RuntimeException("인증 Error, 현재 유저를 찾을 수 없습니다!"));
        User currentUser = twitterUserService.getTwitterUserByUserId(currentUserId);

        return TwitterProfileDto.builder()
                .screenName(currentUser.getScreenName())
                .screenNickname(currentUser.getName())
                .userBio(currentUser.getDescription())
                .profileImageURL(currentUser.get400x400ProfileImageURLHttps())
                .bannerImageURL(currentUser.getProfileBanner1500x500URL())
                .build();
    }
}
