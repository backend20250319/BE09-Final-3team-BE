# Gateway JWT 토큰 추출 테스트 가이드

## 개요
user-service에서 생성된 JWT 토큰이 gateway에서 올바르게 `userNo`와 `userType`을 추출하는지 테스트하는 방법입니다.

## 변경사항 요약

### 1. JwtUtil 개선
- user-service와 동일한 JWT 파싱 방식 적용
- Base64 디코딩된 시크릿 사용
- `typ=access` 클레임 검증 (경고만)
- Number/String 타입 안전 변환
- 디버깅용 `logAllClaims()` 메서드 추가

### 2. AuthenticationFilter 강화
- 상세한 디버깅 로그 추가
- 클레임 추출 실패 시 구체적인 오류 메시지
- 헤더 값 정리 (trim)
- 토큰 정보 로깅 (보안상 일부만)

### 3. 테스트 엔드포인트 추가
- `/api/v1/gateway/health`: 헬스 체크
- `/api/v1/gateway/test-token`: JWT 토큰 분석

## 테스트 방법

### 1. Gateway 서비스 시작
```bash
./gradlew bootRun
```

### 2. user-service에서 JWT 토큰 생성
user-service의 로그인 API를 통해 JWT 토큰을 얻습니다.

### 3. Gateway 테스트

#### 3-1. 헬스 체크
```bash
curl -X GET http://localhost:8000/api/v1/gateway/health
```

#### 3-2. JWT 토큰 테스트
```bash
curl -X GET http://localhost:8000/api/v1/gateway/test-token \
  -H "Authorization: Bearer YOUR_JWT_TOKEN_HERE"
```

### 4. 로그 확인
Gateway 서비스 로그에서 다음 정보를 확인:

```
=== JWT Claims Debug ===
Subject: user@example.com
Expiration: 2025-01-23T...
IssuedAt: 2025-01-23T...
Claim [typ]: access (type: String)
Claim [userNo]: 123 (type: Long)
Claim [userType]: NORMAL (type: String)
========================

Authentication successful - userNo: '123', userType: 'NORMAL', username: 'user@example.com', path: '/api/v1/gateway/test-token'
```

## 예상 응답

### 성공 시
```json
{
  "tokenLength": 245,
  "tokenPreview": "eyJhbGciOiJIUzI1NiJ9...",
  "isValid": true,
  "isExpired": false,
  "extractedClaims": {
    "userNo": "123",
    "userType": "NORMAL", 
    "username": "user@example.com"
  },
  "headerFromFilter": {
    "X-User-No": "123",
    "X-User-Type": "NORMAL"
  }
}
```

### 실패 시
```json
{
  "error": "Token parsing failed: ..."
}
```

## 문제 해결

### 1. 토큰 파싱 실패
- JWT 시크릿이 user-service와 일치하는지 확인
- 토큰 형식이 올바른지 확인 (Bearer 토큰)

### 2. 클레임 누락
- user-service에서 `userNo`, `userType` 클레임이 포함되어 생성되는지 확인
- 토큰 만료 여부 확인

### 3. 헤더 크기 초과
- `application.yml`에서 `server.max-http-header-size: 32KB` 설정 확인
- config server에서도 동일한 설정 적용

## 다음 서비스로 요청 전달 테스트

실제 다운스트림 서비스로 요청이 전달될 때 헤더가 올바르게 추가되는지 확인:

```bash
# 예: user-service로 요청 (실제 라우팅 설정에 따라)
curl -X GET http://localhost:8000/api/v1/user-service/profile \
  -H "Authorization: Bearer YOUR_JWT_TOKEN_HERE"
```

다운스트림 서비스에서 `X-User-No`, `X-User-Type` 헤더를 확인할 수 있습니다.
