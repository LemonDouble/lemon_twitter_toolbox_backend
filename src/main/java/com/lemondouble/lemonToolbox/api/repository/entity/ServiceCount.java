package com.lemondouble.lemonToolbox.api.repository.entity;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
@Data
public class ServiceCount {
    @Id @GeneratedValue
    private Long id;

    private String ServiceName;
    private Long count;
}
