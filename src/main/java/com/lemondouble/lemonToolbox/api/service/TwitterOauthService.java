package com.lemondouble.lemonToolbox.api.service;

import com.lemondouble.lemonToolbox.api.dto.OAuth.TwitterAccessTokenDto;
import com.lemondouble.lemonToolbox.api.repository.OAuthTokenRepository;
import com.lemondouble.lemonToolbox.api.repository.ServiceUserRepository;
import com.lemondouble.lemonToolbox.api.repository.entity.OAuthToken;
import com.lemondouble.lemonToolbox.api.repository.entity.ServiceUser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
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
    private final ServiceUserRepository serviceUserRepository;

    private static final String OAUTH_TYPE = "TWITTER";


    public TwitterOauthService(OAuthTokenRepository oAuthTokenRepository, ServiceUserRepository serviceUserRepository) {
        twitterFactory = new TwitterFactory();
        webClient = WebClient.builder().baseUrl("https://api.twitter.com").build();
        this.oAuthTokenRepository = oAuthTokenRepository;
        this.serviceUserRepository = serviceUserRepository;
    }

    // Request Token : OAUTH 1.0a 로그인을 위한 첫 단계, Login 위한 URL 등 정보 담고 있다.
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

        return new AccessToken(twitterAccessTokenDto.getAccessToken(), twitterAccessTokenDto.getAccessTokenSecret());
    }

    private TwitterAccessTokenDto parseToAccessToken(String responseBodyString) throws TwitterException {
        String[] split = responseBodyString.split("&");

        if(split.length != 4){
            throw new TwitterException("Access token parse failed");
        }

        TwitterAccessTokenDto twitterAccessTokenDto = new TwitterAccessTokenDto();
        twitterAccessTokenDto.setAccessToken(split[0].split("=")[1]);
        twitterAccessTokenDto.setAccessTokenSecret(split[1].split("=")[1]);
        twitterAccessTokenDto.setUserId(split[2].split("=")[1]);
        twitterAccessTokenDto.setScreenName(split[3].split("=")[1]);

        return twitterAccessTokenDto;
    }


    // Access Token 바탕으로 우리 서비스의 User Entity를 리턴.
    // 만약 Access Token이 최신화되어 있지 않다면, Access Token을 최신화한다.
    @Transactional
    public Optional<ServiceUser> findUserByAccessToken(AccessToken accessToken){
        List<OAuthToken> OAuthList = oAuthTokenRepository.findByOauthTypeAndOauthUserId(OAUTH_TYPE, accessToken.getUserId());
        if(OAuthList.isEmpty()){
            return Optional.empty();
        }

        if(OAuthList.size() != 1){
            throw new RuntimeException("findByOauthTypeAndOAuthUserId Error! OAUTH_TYPE = " + OAUTH_TYPE + " , " + "userId = " + accessToken.getUserId());
        }

        // 만약 유저가 트위터에서 Access Token 을 Revoke 했다면, Access Token 값이 다를 수 있다.
        // 이 경우에는 새로 받아온 Access Token으로 최신화 해 준다.

        String token = accessToken.getToken();
        String tokenSecret = accessToken.getTokenSecret();

        if(!OAuthList.get(0).getAccessToken().equals(token) || !OAuthList.get(0).getAccessTokenSecret().equals(tokenSecret)){
            OAuthList.get(0).setAccessToken(token);
            OAuthList.get(0).setAccessTokenSecret(token);
        }

        return Optional.ofNullable(OAuthList.get(0).getServiceUser());
    }

    // 회원가입 되어 있지 않다면, Access Token을 바탕으로 새로 회원가입.
    // User 객체 만들고, OAuthToken 연결시킨다.
    @Transactional
    public ServiceUser registerByAccessToken(AccessToken accessToken){
        // 유저 먼저 회원가입
        ServiceUser user = new ServiceUser();
        ServiceUser savedUser = serviceUserRepository.save(user);

        OAuthToken oAuthToken = OAuthToken.builder()
                .oauthType(OAUTH_TYPE)
                .accessToken(accessToken.getToken())
                .accessTokenSecret(accessToken.getTokenSecret())
                .oauthUserId(accessToken.getUserId())
                .serviceUser(savedUser).build();


        oAuthTokenRepository.save(oAuthToken);

        return savedUser;
    }


    private Twitter getTwitterInstance(){
        Twitter instance = twitterFactory.getInstance();
        instance.setOAuthConsumer(consumerKey, consumerSecret);

        return instance;
    }


}
