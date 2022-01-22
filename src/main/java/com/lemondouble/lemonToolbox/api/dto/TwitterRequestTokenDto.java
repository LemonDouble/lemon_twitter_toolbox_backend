package com.lemondouble.lemonToolbox.api.dto;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TwitterRequestTokenDto {
    private String authenticationURL;
    private String authorizationURL;
    private String token;
    private String tokenSecret;
}
