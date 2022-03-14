# lemon_twitter_toolbox_backend

<p align="center">
  <img src="https://github.com/LemonDouble/lemon_twitter_toolbox_backend/blob/master/readmeLogo.png" />
</p>

Spring boot를 사용한 Lemon Toolbox의 백엔드 서버입니다.

### 주요 기능들은 다음과 같습니다.

1. Spring Security 기반으로 JWT Token 발급 및 인증/인가 시스템 구현
2. Twitter OAuth Token을 기반으로 Access Token 획득 및 DB에 저장, 간편 회원가입 구현
3. Access Token 기반으로 Twitter resources 반환
4. 각 서비스의 CRUD 서비스 제공
5. AWS SQS와 연동, 유저 요청시 Validate 이후 Event Queue에 메세지 전송 및, 작업 완료 Event Queue Listen 이후 훈련 완료 작업 처리
6. Redis 기반의 Atomic counter 구현하여 thread-safe하게 요청 제한 구현

- 번외 : TestContainer와 localstack 이용, AWS나 Redis 설치/연결 여부와 상관 없이 Docker 기반의 독립적 테스트 환경 구축

### 사용된 기술 및 라이브러리는 다음과 같습니다.

- Spring + Spring boot
- Spring Security
- Spring Cloud AWS, Spring Data redis
- jjwt (JWT)
- JPA
- Swagger API
- H2 Database (Develop) , PostgreSQL (Production)
- Testcontainers, Localstack ( for Testing )

### 다음과 같은 문제들이 있었고, 해결 중이거나 해결됐습니다.

( 문제와 해결 방법들을 기술합니다) 

1. 초기 반응이 생각보다 너무 좋아서, Twitter API Limit에 걸리는 문제가 생겼습니다. (해결)
    - 하루 이용자 수 제한을 걸거나, 초대장 시스템을 만드는 방안 중 전자가 효과적이라 판단, 전자를 선택하였습니다.
    - 처음에는 RDB에 Counter table을 만든 뒤, 한 Column에 Transaction을 통해 접근하면 Transaction의 Atomicity 보장 원리를 이용하여 해결할 수 있을 줄 알았습니다.
    - 하지만 실제로는 Race Condition이 발생하여 대기열보다 훨씬 많은 요청이 들어왔고 Transaction의 ACID는 Race Condition이 일어나지 않음을 보장해주지 않음을 알게 되었습니다. (Race Condition이 일어나더라도, ACID는 보장 가능합니다.)
    - 런칭 중이었고 매일 사람이 들어오고 있었으므로, 급한대로 DB의 Exclusive Lock을 이용하였습니다. 이후 nGrinder 등을 이용하여  부하 테스트도 수행한 결과, 예상한 대로 대기열 시스템은 정상적으로 작동하였으나 TPS가 3으로 매우 낮게 떨어지는 문제가 있었습니다. 또한 해당 카운터는 서비스 전체에서 Read가 많이 일어나므로, 수정할 필요가 있었습니다.
    - 따라서 Redis를 도입하여 해당 문제를 해결하였습니다. 싱글 스레드 기반이므로 Atomic counter를 구현할 수 있으면서도, Read 성능을 보장할 수 있었습니다.

1. 프로덕션 환경이므로 Test Code 등을 통해 큰 오류를 미연에 방지하고 싶은데, 시스템이 Twitter API, AWS API, redis 등에 종속적이라 독립적인 테스트 작성이 어려웠습니다. (해결)
    - 가장 먼저 Mocking을 고려하였으나, Redis나 AWS API 등을 전부 처음 사용해 보았으므로 제가 Mocking한 객체가 실제 프로덕션에서 똑같이 작동할 지 보장할 수 없었습니다.
    - 또한, 이미 프로덕션 환경이고 여러 문제가 생기던 중이었기 떄문에, 빠른 대처가 필요했습니다. 따라서, 이미 존재하는 라이브러리를 사용하는 것이 합리적이라는 결론을 내렸습니다.
    - 또한, 고려한 점은 배포의 용이성이었습니다. 서버 비용을 아끼기 위해 Spring 서버는 홈 서버에, ML과 같은 Heavy Task는 Serverless로 구현했는데, 홈 서버에서 (당시) 의문의 Bottleneck이 발생하는 상황이었으므로 만약 필요한 경우 EC2나 로드밸런서를 이용하여 서버를 늘리거나 Spring 서버 자체를 Serverless에 올리는 방안도 고려 중이었습니다. 따라서, 별도의 설정 없이도 git clone만 하면 build가 가능한 환경 구축을 고려하였습니다.
    
    - 결국 Localstack/Testcontainer를 이용하여 외부 서비스를 Mocking 할 수 있었고, 이를 통해 github-actions를 통한 CI, 이후 프로덕션 빌드까지 문제 없이 처리할 수 있었습니다.
    - Junit과의 Integration 또한 강력하여, Junit 테스트 실행 시 관련 Docker Container가 자동으로 실행되고, 테스트 완료 후 자동 종료되어 테스트 수행 시 간편하게 멱등성을 유지할 수 있다는 점도 장점이었습니다.

1. Spring-cloud-aws의 SQS Listener ( Message Consumer ) 가 약 분당 20개밖에 메세지를 처리하지 못 해 Bottleneck이 생겼습니다. (해결 중)
    - https://github.com/awspring/spring-cloud-aws/issues/23 Spring-cloud-aws의 해당 이슈를 보면 Major update 이전까지 수정을 바랄 순 없는 상황이었으므로, 처음 고려한 것은 라이브러리를 들어내고 직접 Aws Sdk를 사용하여 다시 개발하는 것이었습니다.
    - 하지만 이미 프로덕션에 들어간 환경에서 라이브러리 하나를 들어내는 것은 큰 이슈였고, 대안적인 해결 방안으로써 Event Queue에서 메세지를 하나씩 보내는 대신, 10개씩 묶어서 보내는 방안을 고려했습니다.
    - 하지만 실제 테스트 결과 메세지 처리 속도는 하나씩 보낼 때나 묶어서 보낼 때나 크게 달라지지 않았고, 묶어서 보낸 경우 Exception Handling 문제도 발생했습니다. 이벤트를 하나씩 받는 모델의 경우, 에러가 발생하면 단순히 reject 하면 다시 처리할 수 있지만, 묶어서 받는 모델의 경우 메세지를 분해하여 오류 메세지를 다시 모아 재전송해주는 과정을 거쳐야 합니다. 이는 처리 로직을 복잡하게 만들 뿐 아니라, 이후 문제 발생시 트러블슈팅도 힘들게 할 수 있다고 판단하여 원래대로 하나씩 처리하는 모델로 롤백하였습니다.
    - 따라서 다음은 홈 서버에 있는 데이터베이스를 AWS RDB로 옮기고, 처리 완료 메세지를 서버리스로 AWS RDB에서 처리하는 방식을 고려하였으나 이러한 경우 AWS RDB의 예산이 추가 고정 지출로 발생하고, 스프링 서버의 경우 AWS 외부망에서 동작하므로 성능 이슈 등의 문제가 생길 것을 감안하여 유보하였습니다. 대신, 대기열 시스템 개발에 집중하면 하루에 적절한 수준으로 Request를 통제할 수 있을 것이고, Cloudwatch 모니터링 데이터를 바탕으로 대기시간이 크리티컬하지 않은 선까지 요청을 끌어내린 후 추가 개발을 하는 것이 효율적이라 판단하였습니다.
    - 따라서 이후 대기열 시스템을 개발한 뒤 모니터링 결과 Peak time에도 대기 시간이 최대 1~2시간 → 5분 정도로 크게 감소하였고, ML embedding 자체가 Serverless로 병렬적으로 처리해도 10분 정도가 걸림을 고려해 최대 5분까지는 크리티컬하지 않은 대기시간이라 판단, 이후 3.0 라이브러리 릴리즈를 기다리고 있습니다.
