package com.lemondouble.lemonToolbox.jwt;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

@Slf4j
public class JwtFilter extends GenericFilterBean {

    public static final String AUTHORIZATION_HEADER = "Authorization";

    private TokenProvider tokenProvider;

    public JwtFilter(TokenProvider tokenProvider) {
        this.tokenProvider = tokenProvider;
    }

    // JWT Token의 인증 정보를 SecurityContext에 저장하는 역할 수행
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        String jwt = resolveToken(httpServletRequest);
        String requestURI = httpServletRequest.getRequestURI();

        if(StringUtils.hasText(jwt) && tokenProvider.validateToken(jwt)){ // 토큰 검증
            Authentication authentication = tokenProvider.getAuthentication(jwt); // 인증 객체 생성
            SecurityContextHolder.getContext().setAuthentication(authentication); // SecurityContextHolder 에 인증 객체 보냄
            log.debug("Security Context 에 {} 인증 정보를 저장하였습니다. uri : {}", authentication.getName(), requestURI);
        }else {
            log.debug("유효한 JWT Token 이 없습니다. uri : {}", requestURI);
        }

        chain.doFilter(request, response);
    }

    // Request Header에서 토큰 정보를 꺼내오기 위한 resolveToken
    private String resolveToken(HttpServletRequest request){
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
        if(StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")){
            return bearerToken.substring(7);
        }
        return null;
    }
}
