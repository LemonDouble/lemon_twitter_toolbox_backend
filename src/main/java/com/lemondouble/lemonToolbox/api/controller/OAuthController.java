package com.lemondouble.lemonToolbox.api.controller;

import com.lemondouble.lemonToolbox.api.dto.TwitterAccessTokenDto;
import com.lemondouble.lemonToolbox.api.dto.TwitterRequestOauthTokenDto;
import com.lemondouble.lemonToolbox.api.service.TwitterOauthService;
import org.springframework.web.bind.annotation.*;
import twitter4j.TwitterException;
import twitter4j.auth.RequestToken;

@RestController
@RequestMapping("/api/oauth")
public class OAuthController {

    private final TwitterOauthService twitterOauthService;

    public OAuthController(TwitterOauthService twitterOauthService) {
        this.twitterOauthService = twitterOauthService;
    }

    @GetMapping("/request-token")
    public RequestToken getRequestToken() throws TwitterException {
        return twitterOauthService.getRequestToken();
    }


    @PostMapping("/twitter-login-token")
    public TwitterAccessTokenDto authorize(
            @RequestBody TwitterRequestOauthTokenDto twitterRequestOauthTokenDto) throws TwitterException {

        System.out.println("twitterRequestOauthTokenDto = " + twitterRequestOauthTokenDto.toString());

        TwitterAccessTokenDto accessToken = twitterOauthService.getAccessToken(twitterRequestOauthTokenDto.getOauth_token(), twitterRequestOauthTokenDto.getOauth_verifier());
        return accessToken;
    }
}
