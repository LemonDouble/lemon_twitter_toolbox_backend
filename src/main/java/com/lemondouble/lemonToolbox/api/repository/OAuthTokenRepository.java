package com.lemondouble.lemonToolbox.api.repository;

import com.lemondouble.lemonToolbox.api.repository.entity.OAuthToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface OAuthTokenRepository extends JpaRepository<OAuthToken, Long> {
    List<OAuthToken> findByOauthTypeAndOauthUserId(String oauthType, Long oauthUserId);
    List<OAuthToken> findByOauthTypeAndUserId(String oauthType, Long userId);
}
