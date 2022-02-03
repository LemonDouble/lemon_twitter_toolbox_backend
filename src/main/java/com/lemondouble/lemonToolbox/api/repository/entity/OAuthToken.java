package com.lemondouble.lemonToolbox.api.repository.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Getter @Setter
public class OAuthToken {
    protected OAuthToken() {
    }

    public OAuthToken(String oauthType , String accessToken, String accessTokenSecret, Long oauthUserId, ServiceUser user) {
        this.oauthType = oauthType;
        this.accessToken = accessToken;
        this.accessTokenSecret = accessTokenSecret;
        this.oauthUserId = oauthUserId;
        this.user = user;
    }

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "SERVICE_USER_ID")
    private ServiceUser user;

    private String oauthType;

    private String accessToken;

    private String accessTokenSecret;

    @Column(unique = true)
    private Long oauthUserId;
}
