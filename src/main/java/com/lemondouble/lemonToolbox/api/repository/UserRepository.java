package com.lemondouble.lemonToolbox.api.repository;

import com.lemondouble.lemonToolbox.api.repository.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;


public interface UserRepository extends JpaRepository<User, Long> {
}
