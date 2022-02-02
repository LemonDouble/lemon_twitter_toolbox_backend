package com.lemondouble.lemonToolbox.api.repository.entity;

import lombok.*;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "SERVICE_USER")
@Getter @Setter @AllArgsConstructor @NoArgsConstructor
public class User {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "USER_ID")
    private Long id;

    @Column(length = 50)
    private String nickname;

    @OneToMany(mappedBy = "user", cascade = CascadeType.REMOVE)
    private List<OAuthToken> oAuthTokens = new ArrayList<>();
}
