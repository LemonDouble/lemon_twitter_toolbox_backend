package com.lemondouble.lemonToolbox.api.repository;

import com.lemondouble.lemonToolbox.api.repository.entity.ServiceUser;
import org.springframework.data.jpa.repository.JpaRepository;


public interface ServiceUserRepository extends JpaRepository<ServiceUser, Long> {
}
