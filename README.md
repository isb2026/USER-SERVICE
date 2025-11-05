# USER-SERVICE

사용자 인증 및 관리 서비스입니다.

## 기술 스택

- **Spring Boot**: 3.4.1
- **Java**: 17
- **Database**: MySQL (MariaDB)
- **Cache**: Redis
- **Message Queue**: Kafka
- **Security**: Spring Security + JWT
- **API Documentation**: Swagger (SpringDoc OpenAPI)

## 주요 기능

- 사용자 인증 및 권한 관리
- JWT 토큰 기반 인증
- Redis를 활용한 토큰 저장소
- Kafka를 통한 비동기 메시지 처리
- 멀티 테넌트 지원

## 실행 방법

### 로컬 환경
```bash
./gradlew bootRun --args='--spring.profiles.active=local'
```

### 포트
- Local: `8084`
- Dev/Prod: `8080`

### Context Path
`/user`

## API 문서

- Swagger UI: `http://localhost:8084/user/swagger-ui`
- API Docs: `http://localhost:8084/user/v3/api-docs`