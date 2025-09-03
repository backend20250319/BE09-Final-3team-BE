# User Service

사용자 관리 서비스로, 회원가입, 로그인, 프로필 관리 등의 기능을 제공합니다.

## 인증 방식

### 1. JWT 토큰 기반 인증 (기존)
- `/auth/login` 엔드포인트를 통해 JWT 토큰을 발급받아 사용
- `Authorization: Bearer {token}` 헤더로 인증

### 2. 헤더 기반 인증 (새로운 방식)
Gateway에서 JWT 토큰을 파싱하여 사용자 정보를 헤더로 전달하는 방식입니다.

#### 헤더 정보
- `X-User-No`: 사용자 번호 (Long)
- `X-User-Type`: 사용자 타입 (String, 예: "User", "Admin")

#### 장점
- Gateway에서 이미 파싱한 사용자 정보를 재사용하여 성능 향상
- 각 서비스에서 JWT 토큰을 다시 파싱할 필요 없음
- 더 효율적인 인증 처리

## API 엔드포인트

### 공개 엔드포인트 (인증 불필요)
- `POST /auth/signup` - 회원가입
- `POST /auth/login` - 로그인
- `POST /auth/refresh` - 토큰 갱신
- `POST /auth/validate-token` - 토큰 유효성 검증
- `POST /auth/password/reset` - 비밀번호 재설정 요청
- `POST /auth/password/verify` - 비밀번호 재설정 인증번호 확인
- `POST /auth/password/change` - 비밀번호 변경

### 인증 필요 엔드포인트
- `GET /auth/profile` - 프로필 조회
- `PATCH /auth/profile` - 프로필 수정
- `GET /auth/profile/simple?userNo={userNo}` - 간단한 프로필 조회
- `POST /auth/profile/image` - 프로필 이미지 업로드
- `DELETE /auth/withdraw` - 회원탈퇴
- `POST /auth/logout` - 로그아웃
- `GET /auth/me` - 현재 사용자 정보 확인 (테스트용)

## 사용 예시

### 헤더 기반 인증 사용
```bash
# Gateway에서 X-User-No와 X-User-Type 헤더를 설정하여 요청
curl -X GET "http://localhost:8080/auth/profile" \
  -H "X-User-No: 123" \
  -H "X-User-Type: User"
```

### JWT 토큰 기반 인증 사용
```bash
# 기존 방식으로 JWT 토큰을 사용
curl -X GET "http://localhost:8080/auth/profile" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

## 개발 환경 설정

### 필수 요구사항
- Java 17+
- Gradle 7.0+
- Redis (비밀번호 재설정 기능용)

### 실행 방법
```bash
# 프로젝트 빌드
./gradlew build

# 애플리케이션 실행
./gradlew bootRun
```

### 설정 파일
`src/main/resources/application.yml`에서 다음 설정을 확인하세요:
- 데이터베이스 연결 정보
- JWT 시크릿 키
- Redis 연결 정보
- FTP 서버 설정 (프로필 이미지 업로드용)

## 아키텍처 개선 사항

### 기존 문제점
1. Gateway에서 JWT 토큰을 파싱하여 사용자 정보 추출
2. 각 서비스에서 다시 `@RequestAttribute`로 JWT 토큰을 받아서 파싱
3. 중복된 JWT 파싱 작업으로 인한 성능 저하

### 개선된 방식
1. Gateway에서 JWT 토큰을 파싱하여 `X-User-No`, `X-User-Type` 헤더로 전달
2. 각 서비스에서는 헤더에서 직접 사용자 정보를 추출
3. `UserHeaderUtil` 클래스를 통해 간편하게 사용자 정보 접근
4. 성능 향상 및 코드 간소화

### 구현된 클래스
- `UserHeaderUtil`: 헤더에서 사용자 정보를 추출하는 유틸리티 클래스
- `HeaderBasedAuthenticationFilter`: 헤더 기반 인증을 처리하는 필터
- `CustomUserDetailsService.loadUserById()`: 사용자 번호로 UserDetails를 로드하는 메서드
