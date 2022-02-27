package com.lemondouble.lemonToolbox.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.*;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.contexts.SecurityContext;
import springfox.documentation.spring.web.plugins.Docket;

import java.util.Collections;
import java.util.List;

@Configuration
@Profile("dev")
public class SwaggerConfig {

    @Bean
    public Docket api(){

        HttpAuthenticationScheme authenticationScheme = HttpAuthenticationScheme.JWT_BEARER_BUILDER
                .name("Authorization")
                .build();

        return new Docket(DocumentationType.OAS_30)
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.lemondouble.lemonToolbox.api.controller"))
                .paths(PathSelectors.any())
                .build()
                .securityContexts(List.of(securityContext()))
                .securitySchemes(Collections.singletonList(authenticationScheme))
                .apiInfo(apiInfo());
    }

    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("Lemon Toolbox API")
                .description("Lemon Toolbox의 백엔드 API")
                .version("1.0")
                .build();
    }

    private SecurityContext securityContext() {
        return SecurityContext.builder().securityReferences(defaultAuth()).build();
    }

    private List<SecurityReference> defaultAuth() {
        AuthorizationScope authorizationScope = new AuthorizationScope("global", "accessEverything");
        AuthorizationScope[] authorizationScopes = new AuthorizationScope[1];
        authorizationScopes[0] = authorizationScope;
        return List.of(new SecurityReference("Authorization", authorizationScopes));
    }
}
