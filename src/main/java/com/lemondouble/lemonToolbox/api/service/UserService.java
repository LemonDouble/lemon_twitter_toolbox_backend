package com.lemondouble.lemonToolbox.api.service;

import com.lemondouble.lemonToolbox.api.repository.ServiceUserRepository;
import org.springframework.stereotype.Service;


@Service
public class UserService {
    private final ServiceUserRepository serviceUserRepository;

    public UserService(ServiceUserRepository serviceUserRepository) {
        this.serviceUserRepository = serviceUserRepository;
    }

}
