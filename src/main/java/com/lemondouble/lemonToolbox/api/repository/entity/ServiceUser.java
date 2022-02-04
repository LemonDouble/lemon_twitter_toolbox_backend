package com.lemondouble.lemonToolbox.api.repository.entity;

import lombok.*;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter @Setter @AllArgsConstructor @NoArgsConstructor @Builder
public class ServiceUser {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="SERVICE_USER_ID")
    private Long id;

    @Column(length = 50)
    private String nickname;


    @Builder.Default
    @OneToMany(mappedBy = "serviceUser", cascade = CascadeType.REMOVE, fetch = FetchType.LAZY)
    private List<OAuthToken> oAuthTokens = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "serviceUser", cascade = CascadeType.REMOVE, fetch = FetchType.LAZY)
    private List<RegisteredService> registeredServices = new ArrayList<>();
}
