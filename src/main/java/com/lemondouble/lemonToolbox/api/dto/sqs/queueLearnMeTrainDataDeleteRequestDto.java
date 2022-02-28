package com.lemondouble.lemonToolbox.api.dto.sqs;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class queueLearnMeTrainDataDeleteRequestDto {
    private String finished;
    private String userId;
}
