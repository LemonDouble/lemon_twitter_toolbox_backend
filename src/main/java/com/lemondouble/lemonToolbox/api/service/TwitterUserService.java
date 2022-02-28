package com.lemondouble.lemonToolbox.api.service;

import com.lemondouble.lemonToolbox.api.repository.OAuthTokenRepository;
import com.lemondouble.lemonToolbox.api.repository.ServiceUserRepository;
import com.lemondouble.lemonToolbox.api.repository.entity.OAuthToken;
import com.lemondouble.lemonToolbox.api.repository.entity.OAuthType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import twitter4j.*;
import twitter4j.auth.AccessToken;

import java.util.List;

@Service
@Slf4j
public class TwitterUserService {

    @Value("${twitter.consumer-key}")
    private String consumerKey;
    @Value("${twitter.consumer-secret}")
    private String consumerSecret;

    private final TwitterFactory twitterFactory;
    private final OAuthTokenRepository oAuthTokenRepository;
    private final ServiceUserRepository serviceUserRepository;


    public TwitterUserService(OAuthTokenRepository oAuthTokenRepository, ServiceUserRepository serviceUserRepository) {
        twitterFactory = new TwitterFactory();
        this.oAuthTokenRepository = oAuthTokenRepository;
        this.serviceUserRepository = serviceUserRepository;
    }


    /**
     * 우리 서비스의 User id를 바탕으로 Twitter4j의 User 객체를 반환한다.
     */
    public User getTwitterUserByUserId(Long userId) throws TwitterException {
        OAuthToken oAuthToken = getOAuthTokenByUserId(userId);
        Twitter loginedTwitter = getCredentialTwitterInstanceByOAuthToken(oAuthToken);
        return loginedTwitter.showUser(oAuthToken.getOauthUserId());
    }

    /**
     * 우리 서비스의 User id를 바탕으로 Twitter4j의 Twitter 객체를 반환한다.
     */
    public Twitter getCredentialTwitterInstanceByUserId(Long userId) throws TwitterException {
        OAuthToken oAuthToken = getOAuthTokenByUserId(userId);
        return getCredentialTwitterInstanceByOAuthToken(oAuthToken);
    }

    /**
     * 우리 서비스의 User id를 바탕으로 해당 유저의 Twitter Oauth Token 객체를 반환한다.
     */
    public OAuthToken getOAuthTokenByUserId(Long userId) throws RuntimeException{
        List<OAuthToken> findTokens = oAuthTokenRepository.findByOauthTypeAndServiceUserId(OAuthType.TWITTER, userId);
        if(findTokens.size() != 1){
            log.error("TwitterUserService.getOAuthTokenByUserId 무결성 Error, 한 고유 ID에 유저가 두명 이상이거나 없음!! userId = {}",
                    userId);
            throw new RuntimeException("findByOauthTypeAndOAuthUserId Error! OAUTH_TYPE = "+
                    OAuthType.TWITTER +" userId = " + userId);
        }

        return findTokens.get(0);
    }

    /**
     * 유저의 Access Token을 받아, Twitter API에 직접 접근 가능한 Twitter4j의 Twitter instance를 리턴
     */
    private Twitter getCredentialTwitterInstanceByOAuthToken(OAuthToken oAuthToken){
        Twitter defaultTwitterInstance = getDefaultTwitterInstance();
        AccessToken accessToken = new AccessToken(oAuthToken.getAccessToken(), oAuthToken.getAccessTokenSecret());
        defaultTwitterInstance.setOAuthAccessToken(accessToken);
        return defaultTwitterInstance;
    }

    /**
     * Twitter4j의 기본 Twitter Instance를 리턴 ( 유저의 Access Token은 적용되어 있지 않음)
     */
    private Twitter getDefaultTwitterInstance(){
        Twitter instance = twitterFactory.getInstance();
        instance.setOAuthConsumer(consumerKey, consumerSecret);

        return instance;
    }

}
