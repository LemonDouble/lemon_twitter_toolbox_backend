package com.lemondouble.lemonToolbox.api.controller;


import com.lemondouble.lemonToolbox.api.dto.Twitter.TwitterProfileDto;
import com.lemondouble.lemonToolbox.api.service.TwitterUserService;
import com.lemondouble.lemonToolbox.api.util.SecurityUtil;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import twitter4j.TwitterException;
import twitter4j.User;

@RestController
@RequestMapping("/api/twitter")
public class TwitterController {
    private final TwitterUserService twitterUserService;

    public TwitterController(TwitterUserService twitterUserService) {
        this.twitterUserService = twitterUserService;
    }

    /**
     * JWT Token 에서 현재 유저 정보 받아온 뒤 해당 유저의 Profile을 리턴해 준다.<br>
     * screenName -> _lemon_berry_ <br>
     * screenNickname -> 레몬둘 <br>
     * userBio -> 이런저런 것들을 합니다. <br>
     */
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

    private Long getUserId() {
        return SecurityUtil.getCurrentUserId().orElseThrow(()-> new RuntimeException("인증 Error, 현재 유저를 찾을 수 없습니다!"));
    }
}
