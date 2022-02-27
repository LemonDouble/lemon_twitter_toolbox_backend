package com.lemondouble.lemonToolbox.api.repository;

import com.lemondouble.lemonToolbox.api.repository.entity.OAuthToken;
import com.lemondouble.lemonToolbox.api.repository.entity.OAuthType;
import com.lemondouble.lemonToolbox.api.repository.entity.ServiceCount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ServiceCountRepository extends JpaRepository<ServiceCount, Long> {
}