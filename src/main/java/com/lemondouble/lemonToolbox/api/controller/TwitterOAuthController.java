package com.lemondouble.lemonToolbox.api.controller;

import com.lemondouble.lemonToolbox.api.dto.OAuth.TokenDto;
import com.lemondouble.lemonToolbox.api.dto.OAuth.TwitterRequestOauthTokenDto;
import com.lemondouble.lemonToolbox.api.repository.entity.ServiceUser;
import com.lemondouble.lemonToolbox.api.service.TwitterOauthService;
import com.lemondouble.lemonToolbox.jwt.JwtFilter;
import com.lemondouble.lemonToolbox.jwt.TokenProvider;
import io.swagger.annotations.ApiOperation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import twitter4j.TwitterException;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;

import javax.servlet.http.HttpServletResponse;
import java.util.Optional;

@RestController
@RequestMapping("/api/oauth/twitter")
@Tag(name = "twitter-o-auth-controller",description = "트위터 OAuth 인증 관련")
public class TwitterOAuthController {

    private final TwitterOauthService twitterOauthService;
    private final TokenProvider tokenProvider;

    public TwitterOAuthController(TwitterOauthService twitterOauthService, TokenProvider tokenProvider) {
        this.twitterOauthService = twitterOauthService;
        this.tokenProvider = tokenProvider;
    }

    /**
     * OAuth 1.0a에선 처음 로그인 할 때, 해당 App provider 의 Consumer token, secret 이용해 Request Token을 발급받고,
     * 유저가 해당 Request Token 들고 로그인 해야 한다. <br>
     * 해당 Request Token을 발급해 준다. Front 기준으로는, SNS Login URL을 얻을 수 있다.
     */
    @ApiOperation(value = "Request Token 요청")
    @GetMapping("/request-token")
    public RequestToken getRequestToken() throws TwitterException {
        return twitterOauthService.getRequestToken();
    }


    /**
     * 유저가, 발급해준 Request Token 에서 트위터 로그인에 성공했다면 oauthToken, Verifier 를 받아 올 수 있다. <br>
     * 해당 token 과 Verifier 를 이용, Twitter 인증 서버에 가서 해당 유저의 Access Token 과 Secret 받아올 수 있다. <br>
     * Access Token 과 Secret 은 이후 여러 SNS 작업을 할 때 사용되므로 DB에 저장해 둔다. <br>
     * 또한 만약 해당 유저가 우리 서비스에 가입되지 않았다면, 자동으로 해당 Token 기반으로 회원가입 시킨다. <br>
     * (이후 여러 SNS 지원하게 되면, 한 유저에 여러 SNS 연동할 수도 있으므로) <br>
     * 마지막으로, 로그인 시 필요한 JWT TOKEN을 발급해 준다.
     */
    @ApiOperation(value = "Oauth Token 으로 Access Token 서버에 등록 및 로그인",
    notes = "만약 회원가입 되어 있지 않다면, 자동으로 우리 서비스 회원가입도 시킨다.")
    @PostMapping("/twitter-login")
    @ResponseStatus(value = HttpStatus.CREATED)
    public ResponseEntity<TokenDto> authorize(
            @RequestBody TwitterRequestOauthTokenDto twitterRequestOauthTokenDto,
            HttpServletResponse response) throws TwitterException {

        // 받은 OAuth Token 이용해 Access Token 받아온다.
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

        // 로그인용 JWT TOKEN 생성
        String jwtToken = tokenProvider.createToken(registeredUser);

        // 헤더와 바디에 JWT Token 넣어준 뒤 Return
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add(JwtFilter.AUTHORIZATION_HEADER, "Bearer " + jwtToken);

        return new ResponseEntity<>(new TokenDto(jwtToken), httpHeaders, HttpStatus.CREATED);
    }
}
