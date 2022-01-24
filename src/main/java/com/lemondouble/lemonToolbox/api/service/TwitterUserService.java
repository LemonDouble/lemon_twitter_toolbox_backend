package com.lemondouble.lemonToolbox.api.service;

import com.lemondouble.lemonToolbox.api.repository.OAuthTokenRepository;
import com.lemondouble.lemonToolbox.api.repository.UserRepository;
import com.lemondouble.lemonToolbox.api.repository.entity.OAuthToken;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;
import twitter4j.auth.AccessToken;

import java.util.List;

@Service
public class TwitterUserService {

    @Value("${twitter.consumer-key}")
    private String consumerKey;
    @Value("${twitter.consumer-secret}")
    private String consumerSecret;

    private final TwitterFactory twitterFactory;
    private final WebClient webClient;
    private final OAuthTokenRepository oAuthTokenRepository;
    private final UserRepository userRepository;

    private static final String OAUTH_TYPE = "TWITTER";


    public TwitterUserService(OAuthTokenRepository oAuthTokenRepository, UserRepository userRepository) {
        twitterFactory = new TwitterFactory();
        webClient = WebClient.builder().baseUrl("https://api.twitter.com").build();
        this.oAuthTokenRepository = oAuthTokenRepository;
        this.userRepository = userRepository;
    }


    // Twitter4j의 User 객체를 우리 서비스의 User id 통해 반환
    public User getTwitterUserByUserId(Long userId) throws TwitterException {
        OAuthToken oAuthToken = getOAuthTokenByUserId(userId);
        Twitter loginedTwitter = getCredentialTwitterInstanceByOAuthToken(oAuthToken);
        return loginedTwitter.showUser(oAuthToken.getOauthUserId());
    }

    // 유저 ID 기반으로 OAuthToken 객체를 리턴
    private OAuthToken getOAuthTokenByUserId(Long userId) throws RuntimeException{
        List<OAuthToken> findTokens = oAuthTokenRepository.findByOauthTypeAndUserId(OAUTH_TYPE, userId);
        if(findTokens.size() != 1){
            throw new RuntimeException("findByOauthTypeAndOAuthUserId Error! OAUTH_TYPE = "+ OAUTH_TYPE +" userId = " + userId);
        }

        return findTokens.get(0);
    }

    // Access Token을 받아 Twitter API에 직접 접근 가능한 Signed User 리턴
    private Twitter getCredentialTwitterInstanceByOAuthToken(OAuthToken oAuthToken){
        Twitter defaultTwitterInstance = getDefaultTwitterInstance();
        AccessToken accessToken = new AccessToken(oAuthToken.getAccessToken(), oAuthToken.getAccessTokenSecret());
        defaultTwitterInstance.setOAuthAccessToken(accessToken);
        return defaultTwitterInstance;
    }
    
    // App에서 사용할 기본 Twitter Instance를 리턴 ( Access Token은 적용되어 있지 않음)
    private Twitter getDefaultTwitterInstance(){
        Twitter instance = twitterFactory.getInstance();
        instance.setOAuthConsumer(consumerKey, consumerSecret);

        return instance;
    }

}
