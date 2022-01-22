package com.lemondouble.lemonToolbox.api.service;

import com.lemondouble.lemonToolbox.api.dto.TokenDto;
import com.lemondouble.lemonToolbox.api.dto.TwitterAccessTokenDto;
import com.lemondouble.lemonToolbox.api.repository.OAuthTokenRepository;
import com.lemondouble.lemonToolbox.api.repository.UserRepository;
import com.lemondouble.lemonToolbox.api.repository.entity.OAuthToken;
import com.lemondouble.lemonToolbox.api.repository.entity.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;

import java.util.List;
import java.util.Optional;

@Service
public class TwitterOauthService {
    @Value("${twitter.consumer-key}")
    private String consumerKey;
    @Value("${twitter.consumer-secret}")
    private String consumerSecret;

    private final TwitterFactory twitterFactory;
    private final WebClient webClient;
    private final OAuthTokenRepository oAuthTokenRepository;
    private final UserRepository userRepository;

    private static final String OAUTH_TYPE = "TWITTER";


    public TwitterOauthService(OAuthTokenRepository oAuthTokenRepository, UserRepository userRepository) {
        twitterFactory = new TwitterFactory();
        webClient = WebClient.builder().baseUrl("https://api.twitter.com").build();
        this.oAuthTokenRepository = oAuthTokenRepository;
        this.userRepository = userRepository;
    }

    public RequestToken getRequestToken() throws TwitterException {
        Twitter twitterInstance = getTwitterInstance();
        return twitterInstance.getOAuthRequestToken();
    }

    /*  Twitter4j 라이브러리가 항상 Request Token을 같이 보내 줘야 Access Token을 받을 수 있는 문제가 있어 직접 Http 요청한다.

        (로그인시 프론트에서 Redirect 있어 Request Token을 전역 State로 관리하기도 힘들고,
        JWT 사용하므로 서버에서 별도로 Request Token을 들고 있기도 힘들다.)
     */
    public AccessToken getAccessTokenFromOAuthToken(String oauth_token, String oauth_verifier) throws TwitterException {
        String responseBodyString = webClient.post()
                .uri(uriBuilder -> uriBuilder.path("/oauth/access_token")
                        .queryParam("oauth_token", oauth_token)
                        .queryParam("oauth_verifier", oauth_verifier).build())
                .retrieve()
                .bodyToMono(String.class).block();

        TwitterAccessTokenDto twitterAccessTokenDto = parseToAccessToken(responseBodyString);

        return new AccessToken(twitterAccessTokenDto.getAccess_token(), twitterAccessTokenDto.getAccess_token_secret());
    }

    private TwitterAccessTokenDto parseToAccessToken(String responseBodyString) throws TwitterException {
        String[] split = responseBodyString.split("&");

        if(split.length != 4){
            throw new TwitterException("Access token parse failed");
        }

        TwitterAccessTokenDto twitterAccessTokenDto = new TwitterAccessTokenDto();
        twitterAccessTokenDto.setAccess_token(split[0].split("=")[1]);
        twitterAccessTokenDto.setAccess_token_secret(split[1].split("=")[1]);
        twitterAccessTokenDto.setUser_id(split[2].split("=")[1]);
        twitterAccessTokenDto.setScreen_name(split[3].split("=")[1]);

        return twitterAccessTokenDto;
    }

    @Transactional
    public Optional<User> findUserByAccessToken(AccessToken accessToken){
        List<OAuthToken> OAuthList = oAuthTokenRepository.findByOauthTypeAndOauthUserId(OAUTH_TYPE, accessToken.getUserId());
        if(OAuthList.isEmpty()){
            return Optional.empty();
        }

        if(OAuthList.size() != 1){
            throw new RuntimeException("findByOauthTypeAndOAuthUserId 관련 에러! DB 무결성에 오류 생겼음!");
        }

        return Optional.ofNullable(OAuthList.get(0).getUser());
    }

    @Transactional
    public User registerByAccessToken(AccessToken accessToken){
        // 유저 먼저 회원가입
        User user = new User();
        User savedUser = userRepository.save(user);

        OAuthToken oAuthToken = new OAuthToken(OAUTH_TYPE, accessToken.getToken(), accessToken.getTokenSecret(), accessToken.getUserId(), savedUser);
        oAuthTokenRepository.save(oAuthToken);

        return savedUser;
    }


    private Twitter getTwitterInstance(){
        Twitter instance = twitterFactory.getInstance();
        instance.setOAuthConsumer(consumerKey, consumerSecret);

        return instance;
    }


}
