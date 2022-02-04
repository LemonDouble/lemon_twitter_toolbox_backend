package com.lemondouble.lemonToolbox.api.repository.entity;

import lombok.*;

import javax.persistence.*;

@Entity
@Getter @Setter @Builder @AllArgsConstructor @NoArgsConstructor
public class OAuthToken {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "SERVICE_USER_ID")
    private ServiceUser serviceUser;

    private String oauthType;

    private String accessToken;

    private String accessTokenSecret;

    @Column(unique = true)
    private Long oauthUserId;
}
