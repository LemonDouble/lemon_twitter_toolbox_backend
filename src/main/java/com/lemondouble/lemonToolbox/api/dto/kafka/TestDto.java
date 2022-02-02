package com.lemondouble.lemonToolbox.api.dto.kafka;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class TestDto {
    private String AccessKey;
    private String AccessSecret;
    private Long userId;
}
