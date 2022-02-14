package com.lemondouble.lemonToolbox.api.dto.RegisteredService;

import com.lemondouble.lemonToolbox.api.repository.entity.RegisteredService;
import com.lemondouble.lemonToolbox.api.repository.entity.ServiceType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class RegisteredServiceDto {

    public RegisteredServiceDto() {
    }

    public RegisteredServiceDto(RegisteredService registeredService) {
        this.serviceType = registeredService.getServiceType();
        this.isPublic = registeredService.isPublic();
    }

    // 어떤 서비스 사용중인지
    @NotNull
    private ServiceType serviceType;

    @NotNull
    // 해당 서비스를 공개할 것인지? (True면 아무나 볼 수 있게)
    private boolean isPublic;
}
