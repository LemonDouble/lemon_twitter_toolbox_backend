package com.lemondouble.lemonToolbox.entity;

import lombok.*;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table
@Getter @Setter @Builder @AllArgsConstructor @NoArgsConstructor
public class Authority {

    @Id
    @Column(length = 50)
    private String authorityName;
}
