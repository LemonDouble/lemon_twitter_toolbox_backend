package com.lemondouble.lemonToolbox.api.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Objects;
import java.util.Optional;

@Slf4j
public class SecurityUtil {

    public SecurityUtil() {
    }

    // Security Context 의 Authentication 객체를 이용해 username을 리턴해주는 함수
    // Security Context 에 Authentication 저장되는 시점은 JwtFilter의 doFilter 메소드에서 사용됨.
    public static Optional<Long> getCurrentUserId(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if(authentication == null){
            log.debug("Security Context에 인증 정보가 없습니다.");
            return Optional.empty();
        }

        String username = null;
        if(authentication.getPrincipal() instanceof UserDetails){
            UserDetails springSecurityUser = (UserDetails) authentication.getPrincipal();
            username = springSecurityUser.getUsername();
        }else if (authentication.getPrincipal() instanceof String){
            username = (String) authentication.getPrincipal();
        }

        return Optional.of(Long.parseLong(Objects.requireNonNull(username)));
    }
}