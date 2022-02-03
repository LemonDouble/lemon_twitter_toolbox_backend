package com.lemondouble.lemonToolbox.api.repository.entity;

import lombok.*;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table
@Getter @Setter @AllArgsConstructor @NoArgsConstructor
public class ServiceUser {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="SERVICE_USER_ID")
    private Long id;

    @Column(length = 50)
    private String nickname;

    @OneToMany(mappedBy = "user", cascade = CascadeType.REMOVE)
    private List<OAuthToken> oAuthTokens = new ArrayList<>();
}
