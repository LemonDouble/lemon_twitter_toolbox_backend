package com.lemondouble.lemonToolbox.api.service;

import com.lemondouble.lemonToolbox.api.repository.UserRepository;
import com.lemondouble.lemonToolbox.api.repository.entity.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;


// UserDetailsService : JWT Token Provider가 제공한 정보를 바탕으로, DB에서 USER 정보를 가져와 UserDetails 생성
@Component("userDetailsService")
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String id) throws UsernameNotFoundException {
        return userRepository.findById(Long.parseLong(id))
                .map(this::createUser)
                .orElseThrow(()-> new UsernameNotFoundException("user id : " + id + " -> 데이터베이스에서 찾을 수 없습니다"));
    }

    private org.springframework.security.core.userdetails.User createUser(User user){
        return new org.springframework.security.core.userdetails.User(user.getId().toString(), "", null);
    }
}
