package com.lemondouble.lemonToolbox.api.dto.sqs;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class queueNotificationRequestDto {
    private String finished;
    private String AccessToken;
    private String AccessSecret;
    private String userId;
    private String message;
}
