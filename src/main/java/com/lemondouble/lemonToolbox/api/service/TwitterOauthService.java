package com.lemondouble.lemonToolbox.api.service;

import com.lemondouble.lemonToolbox.api.dto.TwitterAccessTokenDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.RequestToken;

@Service
public class TwitterOauthService {
    @Value("${twitter.consumer-key}")
    private String consumerKey;
    @Value("${twitter.consumer-secret}")
    private String consumerSecret;

    private final TwitterFactory twitterFactory;
    private final WebClient webClient;

    public TwitterOauthService() {
        twitterFactory = new TwitterFactory();
        webClient = WebClient.builder().baseUrl("https://api.twitter.com").build();
    }

    public RequestToken getRequestToken() throws TwitterException {
        Twitter twitterInstance = getTwitterInstance();
        return twitterInstance.getOAuthRequestToken();
    }

    /*  Twitter4j 라이브러리가 항상 Request Token을 같이 보내 줘야 Access Token을 받을 수 있는 문제가 있어 직접 Http 요청한다.

        (로그인시 프론트에서 Redirect 있어 Request Token을 전역 State로 관리하기도 힘들고,
        JWT 사용하므로 서버에서 별도로 Request Token을 들고 있기도 힘들다.)
     */
    public TwitterAccessTokenDto getAccessToken(String oauth_token, String oauth_verifier) throws TwitterException {
        String responseBodyString = webClient.post()
                .uri(uriBuilder -> uriBuilder.path("/oauth/access_token")
                        .queryParam("oauth_token", oauth_token)
                        .queryParam("oauth_verifier", oauth_verifier).build())
                .retrieve()
                .bodyToMono(String.class).block();

        TwitterAccessTokenDto twitterAccessTokenDto = parseToAccessToken(responseBodyString);
        return twitterAccessTokenDto;
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


    public Twitter getTwitterInstance(){
        Twitter instance = twitterFactory.getInstance();
        instance.setOAuthConsumer(consumerKey, consumerSecret);

        return instance;
    }


}
