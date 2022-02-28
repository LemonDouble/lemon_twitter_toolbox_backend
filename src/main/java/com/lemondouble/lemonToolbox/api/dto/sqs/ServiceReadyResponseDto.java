package com.lemondouble.lemonToolbox.api.dto.sqs;

import com.lemondouble.lemonToolbox.api.repository.entity.OAuthType;
import com.lemondouble.lemonToolbox.api.repository.entity.ServiceType;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ServiceReadyResponseDto {
    private String finished;
    private Long oAuthUserId;
    private OAuthType oAuthType;
    private ServiceType serviceName;
}
