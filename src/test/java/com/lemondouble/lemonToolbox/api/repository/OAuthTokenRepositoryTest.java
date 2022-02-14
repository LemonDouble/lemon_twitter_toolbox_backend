package com.lemondouble.lemonToolbox.api.repository;

import com.lemondouble.lemonToolbox.api.repository.entity.OAuthToken;
import com.lemondouble.lemonToolbox.api.repository.entity.OAuthType;
import com.lemondouble.lemonToolbox.api.repository.entity.ServiceUser;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class OAuthTokenRepositoryTest {

    @Autowired
    OAuthTokenRepository oAuthTokenRepository;

    @Autowired
    ServiceUserRepository serviceUserRepository;

    @Test
    public void findByOauthTypeAndOauthUserId_성공() throws Exception {
        //given
        ServiceUser user1 = new ServiceUser();
        user1.setNickname("유저1");
        ServiceUser savedUser = serviceUserRepository.saveAndFlush(user1);

        OAuthToken oAuthToken1 = OAuthToken.builder()
                .oauthType(OAuthType.TWITTER)
                .accessToken("abcabc")
                .accessTokenSecret("cbacba")
                .oauthUserId(123123L)
                .serviceUser(savedUser)
                .build();

        OAuthToken savedOAuthToken = oAuthTokenRepository.saveAndFlush(oAuthToken1);
        //when
        List<OAuthToken> tokenList = oAuthTokenRepository.findByOauthTypeAndOauthUserId(OAuthType.TWITTER, 123123L);

        //then

        assertEquals(1, tokenList.size());
        OAuthToken findToken = tokenList.get(0);

        assertEquals(savedOAuthToken, findToken);
        assertEquals(savedUser, findToken.getServiceUser());
    }

    @Test
    public void findByOauthTypeAndOauthUserId_실패_신규가입시() throws Exception {
        //given

        // 유저 회원가입
        ServiceUser user1 = new ServiceUser();
        user1.setNickname("유저1");
        ServiceUser savedUser1 = serviceUserRepository.saveAndFlush(user1);

        // 위의 유저 바탕으로 Oauth Token 추가
        OAuthToken oAuthToken1 = OAuthToken.builder()
                .oauthType(OAuthType.TWITTER)
                .accessToken("abcabc")
                .accessTokenSecret("cbacba")
                .oauthUserId(123123L)
                .serviceUser(savedUser1)
                .build();

        OAuthToken savedOAuthToken = oAuthTokenRepository.saveAndFlush(oAuthToken1);
        //when

        // DB에 있는건 123123L, 111111L은 없어야 함!
        List<OAuthToken> findList = oAuthTokenRepository.findByOauthTypeAndOauthUserId(OAuthType.TWITTER, 111111L);

        //then

        // 찾은 유저가 0명이어야 됨!
        assertEquals(0, findList.size());
    }
}