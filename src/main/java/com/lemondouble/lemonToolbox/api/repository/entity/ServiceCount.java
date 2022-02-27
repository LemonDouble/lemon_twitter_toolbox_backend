package com.lemondouble.lemonToolbox.api.repository.entity;

import lombok.Builder;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity @Data
public class ServiceCount {
    @Id
    private String ServiceName;
    private Long count;

}
