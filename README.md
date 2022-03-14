# lemon_twitter_toolbox_backend

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
- H2 Database (Develop) , PostgreSQL (Production)
- Testcontainers, Localstack ( for Testing )
