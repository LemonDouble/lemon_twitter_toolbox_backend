package com.lemondouble.lemonToolbox.api.repository.entity;

import lombok.*;

import javax.persistence.*;

// Twitter 등의 SNS Token
@Entity
@Getter @Setter @Builder @AllArgsConstructor @NoArgsConstructor
public class OAuthToken {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 어떤 유저와 연결되어 있는지?
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "SERVICE_USER_ID")
    private ServiceUser serviceUser;

    // 이 토큰이 어떤 토큰인지? Twitter, Kakao..
    @Enumerated(EnumType.STRING)
    private OAuthType oauthType;

    // Access Token 과 Secret
    private String accessToken;

    private String accessTokenSecret;

    // 해당 SNS 에서의 해당 유저의 고유 ID. 40243154 같은 것
    @Column(unique = true)
    private Long oauthUserId;
}
