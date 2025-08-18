# Config Service with JWT Authentication

이 프로젝트는 Spring Cloud Config Server에 JWT 인증을 추가한 서비스입니다.

## 기능

- Spring Cloud Config Server
- JWT 기반 인증 및 인가
- Spring Security 통합
- 역할 기반 접근 제어 (RBAC)

## 시작하기

### 1. 환경 변수 설정

다음 환경 변수를 설정하거나 `.env` 파일에 추가하세요:

```bash
# JWT 설정
JWT_SECRET=your-very-long-and-secure-secret-key-for-production
JWT_EXPIRATION=86400000

# Spring Security 사용자 설정
SECURITY_USER_NAME=admin
SECURITY_USER_PASSWORD=password

# Config Server 설정
config_server_rsa=your-private-key
KEYSTORE_PATH=path/to/keystore
KEYSTORE_PASSWORD=keystore-password
KEY_ALIAS=key-alias
```

### 2. 애플리케이션 실행

```bash
./gradlew bootRun
```

애플리케이션은 기본적으로 `http://localhost:8888`에서 실행됩니다.

## API 사용법

### 1. 로그인하여 JWT 토큰 받기

```bash
curl -X POST http://localhost:8888/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "password"
  }'
```

응답:

```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "type": "Bearer"
}
```

### 2. 보호된 엔드포인트 접근

JWT 토큰을 Authorization 헤더에 포함하여 요청:

```bash
curl -X GET http://localhost:8888/api/admin \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

### 3. 사용 가능한 엔드포인트

#### 공개 엔드포인트 (인증 불필요)

- `GET /auth/login` - 로그인
- `GET /api/public` - 공개 정보

#### 인증 필요 엔드포인트

- `GET /api/authenticated` - 인증된 사용자만 접근 가능
- `GET /api/user` - USER 역할 필요
- `GET /api/admin` - ADMIN 역할 필요
- `GET /config/**` - ADMIN 역할 필요 (Config Server 설정)

## 사용자 계정

기본적으로 다음 사용자 계정이 제공됩니다:

### Admin 사용자

- Username: `admin`
- Password: `password`
- Role: `ROLE_ADMIN`

### 일반 사용자

- Username: `user`
- Password: `password`
- Role: `ROLE_USER`

## 보안 설정

### JWT 설정

- **Secret Key**: 환경 변수 `JWT_SECRET`로 설정
- **Expiration**: 환경 변수 `JWT_EXPIRATION`으로 설정 (기본값: 24시간)

### 접근 제어

- `/auth/**`: 모든 사용자 접근 가능
- `/actuator/**`: 모든 사용자 접근 가능
- `/health/**`: 모든 사용자 접근 가능
- `/config/**`: ADMIN 역할만 접근 가능
- 기타 모든 엔드포인트: 인증 필요

## 개발 환경

- Java 17
- Spring Boot 3.5.4
- Spring Security
- Spring Cloud Config Server
- JJWT (JSON Web Token)

## 프로덕션 고려사항

1. **JWT Secret**: 강력하고 안전한 시크릿 키 사용
2. **토큰 만료**: 적절한 만료 시간 설정
3. **HTTPS**: 프로덕션에서는 반드시 HTTPS 사용
4. **사용자 관리**: 실제 데이터베이스 연동 필요
5. **로깅**: 보안 관련 로그 적절히 설정

## 문제 해결

### 토큰 만료

토큰이 만료되면 다시 로그인하여 새로운 토큰을 받으세요.

### 권한 오류

해당 역할이 필요한 엔드포인트에 접근할 때 403 Forbidden 오류가 발생할 수 있습니다.

### 인증 오류

잘못된 토큰이나 만료된 토큰으로 요청 시 401 Unauthorized 오류가 발생합니다.
