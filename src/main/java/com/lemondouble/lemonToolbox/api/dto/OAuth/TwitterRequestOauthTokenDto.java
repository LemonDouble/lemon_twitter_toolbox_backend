package com.lemondouble.lemonToolbox.api.dto.OAuth;

import lombok.Data;

@Data
public class TwitterRequestOauthTokenDto {
    private String oauthToken;
    private String oauthVerifier;
}
