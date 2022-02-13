package com.lemondouble.lemonToolbox.api.repository;

import com.lemondouble.lemonToolbox.api.repository.entity.OAuthToken;
import com.lemondouble.lemonToolbox.api.repository.entity.OAuthType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface OAuthTokenRepository extends JpaRepository<OAuthToken, Long> {
    List<OAuthToken> findByOauthTypeAndOauthUserId(OAuthType oauthType, Long oauthUserId);
    List<OAuthToken> findByOauthTypeAndServiceUserId(OAuthType oauthType, Long serviceUserId);
}
