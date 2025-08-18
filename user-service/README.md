# Petful User Service

Spring Boot 기반의 사용자 인증 서비스입니다. 회원가입과 로그인 기능을 제공하며, JWT 토큰을 사용한 인증을 지원합니다.

## 🚀 기능

- **회원가입**: 이메일, 비밀번호, 이름, 닉네임, 전화번호, 주소, 생년월일을 사용한 회원가입
- **이메일 인증**: 회원가입 시 이메일 인증 기능
- **로그인**: 이메일과 비밀번호를 사용한 로그인
- **JWT 인증**: 로그인 시 JWT 토큰 발급
- **비밀번호 암호화**: BCrypt를 사용한 비밀번호 암호화
- **데이터베이스 저장**: MySQL을 사용한 사용자 정보 저장

## 📋 요구사항

- Java 17
- MySQL 8.0+
- Gradle
- Config Server (별도 서비스)

## 🛠️ 설치 및 실행

### 1. 데이터베이스 설정

MySQL에서 데이터베이스를 생성합니다:

```sql
CREATE DATABASE petful_user;
```

### 2. 애플리케이션 설정

`src/main/resources/application.yml` 파일에서 데이터베이스 연결 정보와 이메일 설정을 수정합니다:

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/petful_user?useSSL=false&serverTimezone=UTC&characterEncoding=UTF-8
    username: your_username
    password: your_password

  mail:
    host: smtp.gmail.com
    port: 587
    username: your-email@gmail.com
    password: your-app-password
    properties:
      mail:
        smtp:
          auth: true
          starttls:
            enable: true
```

### 3. 애플리케이션 실행

```bash
./gradlew bootRun
```

또는

```bash
./gradlew build
java -jar build/libs/user-service-0.0.1-SNAPSHOT.jar
```

## 🌐 API 엔드포인트

### 이메일 인증 발송

```
POST /api/auth/send-verification
Content-Type: application/json

{
  "email": "user@example.com"
}
```

### 이메일 인증 확인

```
POST /api/auth/verify-email
Content-Type: application/json

{
  "email": "user@example.com",
  "verificationCode": "123456"
}
```

### 회원가입

```
POST /api/auth/signup
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "password123",
  "name": "홍길동",
  "nickname": "홍길동",
  "phone": "010-1234-5678",
  "address": "서울시 강남구 테헤란로 123",
  "detailedAddress": "456동 789호",
  "birthYear": 1990,
  "birthMonth": 1,
  "birthDay": 1
}
```

**응답:**

```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "message": "회원가입이 성공적으로 완료되었습니다.",
  "email": "user@example.com",
  "name": "홍길동"
}
```

### 로그인

```
POST /api/auth/login
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "password123"
}
```

**응답:**

```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "message": "로그인이 성공적으로 완료되었습니다.",
  "email": "user@example.com",
  "name": "홍길동"
}
```

### 테스트

```
GET /api/auth/test
```

## 🧪 테스트

브라우저에서 `http://localhost:8080`에 접속하면 회원가입과 로그인을 테스트할 수 있는 웹 페이지가 제공됩니다.

## 📁 프로젝트 구조

```
src/main/java/com/petful/userservice/
├── config/
│   └── SecurityConfig.java          # Spring Security 설정
├── controller/
│   └── UserController.java          # REST API 컨트롤러
├── domain/
│   ├── Role.java                    # 사용자 역할 enum
│   └── User.java                    # 사용자 엔티티
├── dto/
│   ├── AuthResponse.java            # 인증 응답 DTO
│   ├── LoginRequest.java            # 로그인 요청 DTO
│   └── SignupRequest.java           # 회원가입 요청 DTO
├── repository/
│   └── UserRepository.java          # 사용자 데이터 접근 계층
├── security/
│   ├── CustomUserDetailsService.java # 사용자 인증 서비스
│   └── JwtUtil.java                 # JWT 유틸리티
└── service/
    ├── UserService.java             # 사용자 서비스 인터페이스
    └── UserServiceImpl.java         # 사용자 서비스 구현체
```

## 🔐 보안

- **비밀번호 암호화**: BCrypt 알고리즘 사용
- **JWT 토큰**: 24시간 유효한 JWT 토큰 발급
- **CORS**: 모든 도메인에서의 접근 허용 (개발용)
- **CSRF**: 비활성화 (JWT 사용으로 인해)

## 🗄️ 데이터베이스 스키마

애플리케이션 실행 시 자동으로 다음 테이블이 생성됩니다:

```sql
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    name VARCHAR(255) NOT NULL,
    phone VARCHAR(255) NOT NULL,
    role VARCHAR(255) NOT NULL,
    created_at DATETIME,
    updated_at DATETIME
);
```

## 🐛 문제 해결

### 데이터베이스 연결 오류

- MySQL 서비스가 실행 중인지 확인
- 데이터베이스 이름, 사용자명, 비밀번호가 올바른지 확인
- MySQL 포트(3306)가 열려있는지 확인

### 포트 충돌

- 8080 포트가 사용 중인 경우 `application.yml`에서 포트를 변경

## 📝 라이선스

이 프로젝트는 MIT 라이선스 하에 배포됩니다.
