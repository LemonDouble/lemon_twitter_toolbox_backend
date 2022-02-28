package com.lemondouble.lemonToolbox.api.dto.RegisteredService;

import lombok.Builder;
import lombok.Data;

@Data @Builder
public class LearnMeCanUseResponseDto {
    private boolean canUse;
    private Long registerCount;
    private Long registerLimit;
}
