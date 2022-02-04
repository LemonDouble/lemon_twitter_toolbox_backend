package com.lemondouble.lemonToolbox.api.repository;

import com.lemondouble.lemonToolbox.api.repository.entity.RegisteredService;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RegisteredServiceRepository extends JpaRepository<RegisteredService, Long> {
}
