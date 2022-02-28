package com.lemondouble.lemonToolbox.api.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.lemondouble.lemonToolbox.api.dto.RegisteredService.LearnMeCanUseResponseDto;
import com.lemondouble.lemonToolbox.api.dto.RegisteredService.LearnMeRegisterResponseDto;
import com.lemondouble.lemonToolbox.api.dto.RegisteredService.RegisteredServiceModifyDto;
import com.lemondouble.lemonToolbox.api.dto.RegisteredService.RegisteredServiceResponseDto;
import com.lemondouble.lemonToolbox.api.repository.entity.OAuthToken;
import com.lemondouble.lemonToolbox.api.repository.entity.RegisteredService;
import com.lemondouble.lemonToolbox.api.repository.entity.ServiceType;
import com.lemondouble.lemonToolbox.api.service.RegisteredServiceService;
import com.lemondouble.lemonToolbox.api.service.ServiceCountService;
import com.lemondouble.lemonToolbox.api.service.SqsMessageService;
import com.lemondouble.lemonToolbox.api.service.TwitterUserService;
import com.lemondouble.lemonToolbox.api.util.SecurityUtil;
import io.swagger.annotations.ApiOperation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/service")
@Tag(name = "registered-service-controller",description = "LearnMe, 트윗청소기 등 서비스 관련")
public class RegisteredServiceController {

    private final RegisteredServiceService registeredServiceService;
    private final TwitterUserService twitterUserService;
    private final SqsMessageService sqsMessageService;
    private final ServiceCountService serviceCountService;


    public RegisteredServiceController(
            RegisteredServiceService registeredServiceService,
            TwitterUserService twitterUserService,
            SqsMessageService sqsMessageService,
            ServiceCountService serviceCountService) {
        this.registeredServiceService = registeredServiceService;
        this.twitterUserService = twitterUserService;
        this.sqsMessageService = sqsMessageService;
        this.serviceCountService = serviceCountService;
    }


    /**
     * 해당 유저가 가입하고 있는 서비스를 DB에서 조회하고 보여준다.
     */

    @ApiOperation(value = "현재 가입하고 있는 서비스 조회")
    @GetMapping()
    public ResponseEntity<List<RegisteredServiceResponseDto>> getMyRegisteredService(){
        Long currentId = getUserId();

        List<RegisteredService> registeredServices =
                registeredServiceService.getMyRegisteredServicesByUserId(currentId);

        List<RegisteredServiceResponseDto> responseDto = registeredServices.stream().map(
                RegisteredServiceResponseDto::new
        ).collect(Collectors.toList());

        return new ResponseEntity<>(responseDto, HttpStatus.OK);
    }

    @ApiOperation(value = "현재 가입하고 있는 서비스 상태 수정")
    @PutMapping()
    public ResponseEntity<RegisteredServiceModifyDto> setMyRegisteredService(@RequestBody @Valid RegisteredServiceModifyDto registeredServiceModifyDto){
        Long currentId = getUserId();

        RegisteredService modifiedRegisteredService =
                registeredServiceService.modifyServiceInfoByUserIdAndServiceDto(currentId, registeredServiceModifyDto);

        RegisteredServiceModifyDto responseDto = new RegisteredServiceModifyDto(modifiedRegisteredService);

        return new ResponseEntity<>(responseDto, HttpStatus.OK);
    }

    /**
     * 해당 유저의 JWT 바탕으로 Learn Me 서비스에 등록시킨다. <br>
     * 이후, SQS 에 서비스 요청 날려 해당 유저의 ML 데이터 준비한다. <br>
     * 아래 프로세스가 순차적으로 실행된다. <br>
     * Access Token, Secret 과 User Id 실어서 보내면 AWS Lambda 에 의해 다음 처리가 순차적으로 실행된다. <br>
     * 1. 해당 유저의 트윗을 3200개 (Limit) 까지 가져온다. <br>
     * 2. 나한테 온 멘션을 Question, 내가 보낸 답멘을 Answer 로 정리한다. <br>
     * 3. 다음 Pipeline 에 의해 중복 Tweet 은 제거된다. <br>
     * 4. Bert Model 을 통해 해당 트윗들을 Embedding 해 S3에 .pickle 로 저장한다. <br>
     * 5. 작업 완료시 트위터에 알람을 보낸다. <br>
     */
    @ApiOperation(value = "Learn Me 서비스 등록 및 사용")
    @ResponseStatus(value = HttpStatus.CREATED)
    @PostMapping("/learn_me")
    public ResponseEntity<LearnMeRegisterResponseDto> usingLearnMe() throws JsonProcessingException {
        Long currentId = getUserId();

        // 현재 유저의 oAuthToken 으로 SQS Queue 에 서비스 요청 날린다.
        // 만약 Exception 발생하면 나가진다.
        OAuthToken oAuthToken = twitterUserService.getOAuthTokenByUserId(currentId);
        LearnMeRegisterResponseDto responseDto = sqsMessageService.sendToRequestTweetQueue(oAuthToken);

        // 현재 유저를 learn Me 서비스에 가입시킨다
        // 단, 중복 가입은 안 되므로, 이미 가입되어 있다면 무시된다.
        registeredServiceService.joinLearnMe(currentId);

        // 다음 사용 가능 시간을 일주일 뒤 이 시간으로 변경
        registeredServiceService.setNextUseTime(currentId, ServiceType.LEARNME, LocalDateTime.now().plusDays(7));

        return new ResponseEntity<>(LearnMeRegisterResponseDto.builder().registerCount(1L).registerLimit(300L).build(), HttpStatus.CREATED);
    }

    @ApiOperation(value = "Learn Me 서비스 사용 가능 여부 조회(하루 Limit 수 넘어갔는지?)")
    @GetMapping("/learn_me/can-use")
    public ResponseEntity<LearnMeCanUseResponseDto> checkCanUseLearnMe(){
        LearnMeCanUseResponseDto responseDto = serviceCountService.canUseLearnMeService();

        return new ResponseEntity<>(responseDto, HttpStatus.OK);
    }

    @ApiOperation(value = "Learn Me 탈퇴 및 데이터 삭제")
    @DeleteMapping("/learn_me")
    public ResponseEntity<Void> unRegisterLearnMe() throws JsonProcessingException {
        Long currentId = getUserId();

        // 현재 유저의 oAuthToken 으로 SQS Queue 에 서비스 요청 날린다.
        OAuthToken oAuthToken = twitterUserService.getOAuthTokenByUserId(currentId);
        sqsMessageService.sendToLearnMeTrainDataDeleteRequestQueue(oAuthToken);

        // 현재 유저를 learn Me 서비스에서 탈퇴시킨다.
        // 단, 이미 탈퇴되어 있다면 무시된다.
        registeredServiceService.unJoinLearnMe(currentId);

        return new ResponseEntity<>(HttpStatus.OK);
    }

    private Long getUserId() {
        return SecurityUtil.getCurrentUserId().orElseThrow(()-> new RuntimeException("인증 Error, 현재 유저를 찾을 수 없습니다!"));
    }
}
