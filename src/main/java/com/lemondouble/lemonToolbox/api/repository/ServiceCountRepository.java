package com.lemondouble.lemonToolbox.api.repository;

import com.lemondouble.lemonToolbox.api.repository.entity.OAuthToken;
import com.lemondouble.lemonToolbox.api.repository.entity.OAuthType;
import com.lemondouble.lemonToolbox.api.repository.entity.ServiceCount;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import javax.persistence.LockModeType;
import javax.swing.text.html.Option;
import java.util.List;
import java.util.Optional;

public interface ServiceCountRepository extends JpaRepository<ServiceCount, String> {

    @Lock(LockModeType.PESSIMISTIC_READ)
    @Query(value = "select s from ServiceCount s where s.ServiceName = :serviceName")
    Optional<ServiceCount> findByServiceCountForUpdate(@Param("serviceName") String serviceName);
}