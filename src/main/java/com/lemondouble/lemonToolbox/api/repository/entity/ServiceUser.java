package com.lemondouble.lemonToolbox.api.repository.entity;

import lombok.*;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

// 이 서비스를 사용하고 있는 유저 Entity
@Entity
@Getter @Setter @AllArgsConstructor @NoArgsConstructor @Builder
public class ServiceUser {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="SERVICE_USER_ID")
    private Long id;

    // 닉네임!
    @Column(length = 50)
    private String nickname;

    // 해당 유저와 연결되어있는 oAuth Token들, 지금은 Twitter밖에 없다.
    @Builder.Default
    @OneToMany(mappedBy = "serviceUser", cascade = CascadeType.REMOVE, fetch = FetchType.LAZY)
    private List<OAuthToken> oAuthTokens = new ArrayList<>();

    // 해당 유저가 사용 중인 서비스 (자동 트윗청소기 등..)
    @Builder.Default
    @OneToMany(mappedBy = "serviceUser", cascade = CascadeType.REMOVE, fetch = FetchType.LAZY)
    private List<RegisteredService> registeredServices = new ArrayList<>();
}
