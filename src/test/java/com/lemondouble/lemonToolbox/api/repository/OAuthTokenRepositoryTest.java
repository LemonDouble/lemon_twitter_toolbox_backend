package com.lemondouble.lemonToolbox.api.repository;

import com.lemondouble.lemonToolbox.api.repository.entity.OAuthToken;
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

        OAuthToken oAuthToken1 = new OAuthToken("TWITTER","abcabc", "abcabc", 123123L, savedUser);

        OAuthToken savedOAuthToken = oAuthTokenRepository.saveAndFlush(oAuthToken1);
        //when
        List<OAuthToken> tokenList = oAuthTokenRepository.findByOauthTypeAndOauthUserId("TWITTER", 123123L);

        //then

        assertEquals(1, tokenList.size());
        OAuthToken findToken = tokenList.get(0);

        assertEquals(savedOAuthToken, findToken);
        assertEquals(savedUser, findToken.getUser());
    }

    @Test
    public void findByOauthTypeAndOauthUserId_실패_신규가입시() throws Exception {
        //given
        ServiceUser user1 = new ServiceUser();
        user1.setNickname("유저1");
        ServiceUser user2 = new ServiceUser();
        user1.setNickname("유저2");
        ServiceUser savedUser1 = serviceUserRepository.saveAndFlush(user1);
        ServiceUser savedUser2 = serviceUserRepository.saveAndFlush(user2);

        OAuthToken oAuthToken1 = new OAuthToken("TWITTER","abcabc", "abcabc", 123123L, savedUser1);
        OAuthToken savedOAuthToken = oAuthTokenRepository.saveAndFlush(oAuthToken1);
        //when
        List<OAuthToken> findList = oAuthTokenRepository.findByOauthTypeAndOauthUserId("TWITTER", 111111L); // 신규 가입자의 경우 (DB에 없음)

        //then
        assertEquals(0, findList.size());
    }
}