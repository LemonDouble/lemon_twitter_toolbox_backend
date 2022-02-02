package com.lemondouble.lemonToolbox.config;

import com.lemondouble.lemonToolbox.jwt.JwtAccessDeniedHandler;
import com.lemondouble.lemonToolbox.jwt.JwtAuthenticationEntryPoint;
import com.lemondouble.lemonToolbox.jwt.JwtSecurityConfig;
import com.lemondouble.lemonToolbox.jwt.TokenProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.filter.CorsFilter;

// Web security 관련 설정
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    private final TokenProvider tokenProvider;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final JwtAccessDeniedHandler jwtAccessDeniedHandler;
    private final CorsFilter corsFilter;

    public SecurityConfig(
            TokenProvider tokenProvider,
            JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint,
            JwtAccessDeniedHandler jwtAccessDeniedHandler,
            CorsFilter corsFilter
    ){
        this.tokenProvider = tokenProvider;
        this.jwtAuthenticationEntryPoint = jwtAuthenticationEntryPoint;
        this.jwtAccessDeniedHandler = jwtAccessDeniedHandler;
        this.corsFilter = corsFilter;
    }

    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }


    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                // Token 사용하므로 disable 해 주고,
                .csrf().disable()

                // CORS 필터 설정
                .addFilterBefore(corsFilter, UsernamePasswordAuthenticationFilter.class)

                //exceptionHandler 직접 만든 jwtHandler 추가
                .exceptionHandling()
                .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                .accessDeniedHandler(jwtAccessDeniedHandler)

                // H2 database options
                .and()
                .headers()
                .frameOptions()
                .sameOrigin()

                // Token 기반 인증 사용하므로 session Disable( SessionCreationPolicy.STATELESS : Session 만들지 않음)
                .and()
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)

                // /api/hello, authenticate, signup 만 허용, 나머지는 비허용
                .and()
                .authorizeRequests()
                .antMatchers("/api/test").permitAll()
                .antMatchers("/api/authenticate").permitAll()
                .antMatchers("/api/signup").permitAll()
                .antMatchers("/api/oauth/**").permitAll()
                .anyRequest().authenticated()

                // JWT filter 적용한 JwtSecurityConfig 클래스 적용
                .and()
                .apply(new JwtSecurityConfig(tokenProvider));
    }

    /* 개발용 : H2 DB 관련 요청은 Security 설정 상관없이 접근 가능하게 */
    @Override
    public void configure(WebSecurity web) {
        web
                .ignoring()
                .antMatchers("/h2-console/**", "/favicon.ico");
    }
}
