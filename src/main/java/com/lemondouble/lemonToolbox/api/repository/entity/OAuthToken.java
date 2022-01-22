package com.lemondouble.lemonToolbox.api.repository.entity;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Getter @Setter
public class OAuthToken {
    protected OAuthToken() {
    }

    public OAuthToken(String oauthType , String accessToken, String accessTokenSecret, Long oauthUserId, User user) {
        this.oauthType = oauthType;
        this.accessToken = accessToken;
        this.accessTokenSecret = accessTokenSecret;
        this.oauthUserId = oauthUserId;
        this.user = user;
    }

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "OAUTH_ID")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "USER_ID")
    private User user;

    private String oauthType;

    private String accessToken;

    private String accessTokenSecret;

    @Column(unique = true)
    private Long oauthUserId;
}
