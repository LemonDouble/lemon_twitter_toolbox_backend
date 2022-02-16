package com.lemondouble.lemonToolbox.api.dto.RegisteredService;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.lemondouble.lemonToolbox.api.repository.entity.RegisteredService;
import com.lemondouble.lemonToolbox.api.repository.entity.ServiceType;
import lombok.Data;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
public class RegisteredServiceResponseDto {

    public RegisteredServiceResponseDto() {
    }

    public RegisteredServiceResponseDto(RegisteredService registeredService) {
        this.serviceType = registeredService.getServiceType();
        this.isPublic = registeredService.isPublic();
        this.isReady = registeredService.isReady();
        this.canUseTime = registeredService.getCanUseTime();
    }
    // 어떤 서비스 사용중인지
    @NotNull
    private ServiceType serviceType;

    @NotNull
    // 해당 서비스를 공개할 것인지? (True면 아무나 볼 수 있게)
    private boolean isPublic;

    // 해당 서비스가 지금 준비되어 있는지?
    @NotNull
    private boolean isReady;

    // 해당 서비스 다음 사용가능 시간이 언제인지?
    @NotNull
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime canUseTime;
}
