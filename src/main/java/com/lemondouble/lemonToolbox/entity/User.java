package com.lemondouble.lemonToolbox.entity;

import lombok.*;

import javax.persistence.*;
import java.util.Set;

@Entity
@Table
@Getter @Setter @Builder @AllArgsConstructor @NoArgsConstructor
public class User {

    @Id @Column
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    @Column(length = 50, unique = true)
    private String username;

    @Column(length = 100)
    private String password;

    @Column(length = 50)
    private String nickname;

    @Column()
    private boolean activated;

    @ManyToMany
    @JoinTable(
            name = "USER_AUTHORITY",
            joinColumns = {@JoinColumn(name = "userId")},
            inverseJoinColumns = {@JoinColumn(name = "authorityName")})
    private Set<Authority> authorities;
}
