package com.lemondouble.lemonToolbox.api.dto.OAuth;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TwitterAccessTokenDto {
    private String accessToken;
    private String accessTokenSecret;
    private String userId;
    private String screenName;
}
