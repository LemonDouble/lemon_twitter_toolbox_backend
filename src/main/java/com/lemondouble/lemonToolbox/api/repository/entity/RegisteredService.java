package com.lemondouble.lemonToolbox.api.repository.entity;

import lombok.*;

import javax.persistence.*;

// 이 서비스에서 가입하고 있는 서비스 Entity
@Entity
@Getter @Setter @AllArgsConstructor @NoArgsConstructor @Builder
public class RegisteredService {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 어떤 유저인지
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "SERVICE_USER_ID")
    private ServiceUser serviceUser;

    // 어떤 서비스 사용중인지
    @Enumerated(EnumType.STRING)
    private ServiceType serviceType;
}
