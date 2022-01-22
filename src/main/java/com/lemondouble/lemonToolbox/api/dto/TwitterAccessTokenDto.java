package com.lemondouble.lemonToolbox.api.dto;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TwitterAccessTokenDto {
    private String access_token;
    private String access_token_secret;
    private String user_id;
    private String screen_name;
}
