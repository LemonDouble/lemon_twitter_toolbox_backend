package com.lemondouble.lemonToolbox.api.dto;

import lombok.Data;

@Data
public class TwitterRequestOauthTokenDto {
    private String oauth_token;
    private String oauth_verifier;
}
