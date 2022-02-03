package com.lemondouble.lemonToolbox.api.controller;

import com.lemondouble.lemonToolbox.api.dto.OAuth.TokenDto;
import com.lemondouble.lemonToolbox.api.dto.OAuth.TwitterRequestOauthTokenDto;
import com.lemondouble.lemonToolbox.api.repository.entity.ServiceUser;
import com.lemondouble.lemonToolbox.api.service.TwitterOauthService;
import com.lemondouble.lemonToolbox.jwt.JwtFilter;
import com.lemondouble.lemonToolbox.jwt.TokenProvider;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.web.bind.annotation.*;
import twitter4j.TwitterException;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;

import javax.servlet.http.HttpServletResponse;
import java.util.Optional;

@RestController
@RequestMapping("/api/oauth")
public class OAuthController {

    private final TwitterOauthService twitterOauthService;
    private final TokenProvider tokenProvider;

    public OAuthController(TwitterOauthService twitterOauthService, TokenProvider tokenProvider, AuthenticationManagerBuilder authenticationManagerBuilder) {
        this.twitterOauthService = twitterOauthService;
        this.tokenProvider = tokenProvider;
    }

    @GetMapping("/request-token")
    public RequestToken getRequestToken() throws TwitterException {
        return twitterOauthService.getRequestToken();
    }


    @PostMapping("/twitter-login")
    public ResponseEntity<TokenDto> authorize(
            @RequestBody TwitterRequestOauthTokenDto twitterRequestOauthTokenDto,
            HttpServletResponse response) throws TwitterException {

        // 보내준 OAuth Token 이용해 Access Token 받아온다.
        AccessToken accessToken = twitterOauthService.getAccessTokenFromOAuthToken(
                twitterRequestOauthTokenDto.getOauthToken(),
                twitterRequestOauthTokenDto.getOauthVerifier());

        ServiceUser registeredUser;

        Optional<ServiceUser> userByAccessToken = twitterOauthService.findUserByAccessToken(accessToken);

        // Access token과 매치되는 유저가 없는 경우 새로 회원가입 시킨다.
        if(userByAccessToken.isEmpty()){
            registeredUser = twitterOauthService.registerByAccessToken(accessToken);
        }else{
            registeredUser = userByAccessToken.get();
        }

        String jwtToken = tokenProvider.createToken(registeredUser);

        // 헤더와 바디에 JWT Token 넣어준 뒤 Return
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add(JwtFilter.AUTHORIZATION_HEADER, "Bearer " + jwtToken);

        return new ResponseEntity<>(new TokenDto(jwtToken), httpHeaders, HttpStatus.OK);
    }
}
