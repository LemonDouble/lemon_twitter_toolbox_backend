package com.lemondouble.lemonToolbox.api.service;

import com.lemondouble.lemonToolbox.api.dto.OAuth.TwitterAccessTokenDto;
import com.lemondouble.lemonToolbox.api.repository.OAuthTokenRepository;
import com.lemondouble.lemonToolbox.api.repository.ServiceUserRepository;
import com.lemondouble.lemonToolbox.api.repository.entity.OAuthToken;
import com.lemondouble.lemonToolbox.api.repository.entity.OAuthType;
import com.lemondouble.lemonToolbox.api.repository.entity.ServiceUser;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class TwitterOauthService {
    @Value("${twitter.consumer-key}")
    private String consumerKey;
    @Value("${twitter.consumer-secret}")
    private String consumerSecret;

    private final TwitterFactory twitterFactory;
    private final WebClient webClient;
    private final OAuthTokenRepository oAuthTokenRepository;
    private final ServiceUserRepository serviceUserRepository;


    public TwitterOauthService(OAuthTokenRepository oAuthTokenRepository, ServiceUserRepository serviceUserRepository) {
        twitterFactory = new TwitterFactory();
        webClient = WebClient.builder().baseUrl("https://api.twitter.com").build();
        this.oAuthTokenRepository = oAuthTokenRepository;
        this.serviceUserRepository = serviceUserRepository;
    }

    /**
     * Request Token을 반환한다. <br>
     * Request Token : OAUTH 1.0a 로그인을 위한 첫 단계, Login 위한 URL 등 정보 담고 있다.
     */
    public RequestToken getRequestToken() throws TwitterException {
        Twitter twitterInstance = getTwitterInstance();
        return twitterInstance.getOAuthRequestToken();
    }

     /**
      * oauth token과 secret 받아 Twitter에 요청 후, Access Token 반환한다. <br> <br>
      * Twitter API는 OAuth Token만 있어도 Access Token을 발급해 주지만, <br>
      * Twitter4j 라이브러리는 항상 Request Token을 같이 보내 줘야 Access Token을 받을 수 있는 문제가 있어 직접 Http 요청한다. <br>
      * 로그인시 Redirect 일어나므로 프론트엔드에서 전역 상태로 Request Token을 항상 들고 있기도 힘들고, <br>
      * 백엔드에서도 State를 하나라도 줄이기 위해 굳이 Request Token을 들고있을 이유가 없다.
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

    /**
     * 트위터에서 받아온 Response String 을 AccessTokenDto 로 Parsing 한다.
     */
    private TwitterAccessTokenDto parseToAccessToken(String responseBodyString) throws TwitterException {
        String[] split = responseBodyString.split("&");

        if(split.length != 4){
            log.error("TwitterOauthService.parseToAccessToken parse Error!! API 바뀌었을 수 있음!" +
                    " responseBody = {}", responseBodyString);
            throw new TwitterException("Access token parse failed");
        }

        TwitterAccessTokenDto twitterAccessTokenDto = new TwitterAccessTokenDto();
        twitterAccessTokenDto.setAccessToken(split[0].split("=")[1]);
        twitterAccessTokenDto.setAccessTokenSecret(split[1].split("=")[1]);
        twitterAccessTokenDto.setUserId(split[2].split("=")[1]);
        twitterAccessTokenDto.setScreenName(split[3].split("=")[1]);

        return twitterAccessTokenDto;
    }


    /**
     * Access Token을 바탕으로 우리 서비스의 ServiceUser Entity 를 반환한다. <br> <br>
     *
     * 만약 Access Token이 만료되었거나 최신화되어있지 않다면, Access Token을 최신화한다. <br>
     */
    @Transactional
    public Optional<ServiceUser> findUserByAccessToken(AccessToken accessToken){

        // Twitter OAuth Token 중에서, 고유 ID가 받은 Token과 같은 유저를 찾는다.
        // Twitter 내에서 유저의 고유 ID는 고정되어 있기 때문에, 이와 같이 하면 항상 같은 유저가 찾아짐이 보장된다.
        List<OAuthToken> OAuthList = oAuthTokenRepository.findByOauthTypeAndOauthUserId(OAuthType.TWITTER, accessToken.getUserId());

        // 만약 가입한 적 없다면, empty 리턴한다.
        if(OAuthList.isEmpty()){
            return Optional.empty();
        }

        // 일어나면 안 되는 경우, 만약 한 고유 ID에 2 Column 이상이 매칭된다면 DB 무결성에 에러가 난 경우이다.
        if(OAuthList.size() != 1){
            log.error("TwitterOauthService.findUserByAccessToken 무결성 Error, 한 고유 ID에 유저가 두명 이상! userId = {}",
                    accessToken.getUserId());

            throw new RuntimeException("findByOauthTypeAndOAuthUserId Error! OAUTH_TYPE = " + OAuthType.TWITTER + " , "
                    + "userId = " + accessToken.getUserId());
        }

        // 만약 유저가 트위터에서 Access Token 을 Revoke 했다면, Access Token 값이 다를 수 있다.
        // 이 경우에는 새로 받아온 Access Token으로 최신화 해 준다.
        String token = accessToken.getToken();
        String tokenSecret = accessToken.getTokenSecret();

        if(!OAuthList.get(0).getAccessToken().equals(token) || !OAuthList.get(0).getAccessTokenSecret().equals(tokenSecret)){
            log.info("findUserByAccessToken : Access Token Revoke 되어 최신화");
            log.info("before: token={} , secret={}", OAuthList.get(0).getAccessToken(), OAuthList.get(0).getAccessTokenSecret());
            log.info("after: token={} , secret={}", accessToken.getToken(), accessToken.getTokenSecret());
            Long oAuthTokenId = OAuthList.get(0).getId();
            OAuthToken oAuthToken = oAuthTokenRepository.findById(oAuthTokenId)
                    .orElseThrow(()-> {throw new RuntimeException("findUserByAccessToken -> 최신화 하려 했는데 Token이 없습니다??");});
            oAuthToken.setAccessToken(token);
            oAuthToken.setAccessTokenSecret(tokenSecret);
            oAuthTokenRepository.save(oAuthToken);
        }

        // 해당 유저 반환
        return Optional.ofNullable(OAuthList.get(0).getServiceUser());
    }


    /**
     * Access Token을 바탕으로 우리 서비스에 가입시키고, Service User Entity를 반환한다. <br><br>
     *
     * User Entity를 먼저 만들고, OAuth Token을 연결시킨다.
     */
    @Transactional
    public ServiceUser registerByAccessToken(AccessToken accessToken){
        // 유저 먼저 회원가입
        ServiceUser user = new ServiceUser();
        ServiceUser savedUser = serviceUserRepository.save(user);

        OAuthToken oAuthToken = OAuthToken.builder()
                .oauthType(OAuthType.TWITTER)
                .accessToken(accessToken.getToken())
                .accessTokenSecret(accessToken.getTokenSecret())
                .oauthUserId(accessToken.getUserId())
                .serviceUser(savedUser).build();

        oAuthTokenRepository.save(oAuthToken);

        return savedUser;
    }

    /**
     * application.yml 바탕으로 Twitter4j에서 사용하는 Twitter Instance 받아오는 함수
     */

    private Twitter getTwitterInstance(){
        Twitter instance = twitterFactory.getInstance();
        instance.setOAuthConsumer(consumerKey, consumerSecret);

        return instance;
    }


}
