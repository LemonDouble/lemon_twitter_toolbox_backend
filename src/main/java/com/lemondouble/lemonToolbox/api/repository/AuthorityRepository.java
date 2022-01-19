package com.lemondouble.lemonToolbox.api.repository;

import com.lemondouble.lemonToolbox.entity.Authority;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuthorityRepository extends JpaRepository<Authority, String> {
}
