# SNS Service API Tests

이 폴더는 SNS Service의 모든 HTTP API를 테스트하기 위한 `.http` 파일들을 포함합니다.

## 📁 파일 구조

```
📁 api-tests/
├── 📄 01-instagram-auth.http  # Instagram 인증 API
├── 📄 02-instagram-profile.http # Instagram 프로필 API
├── 📄 03-instagram-media.http # Instagram 미디어 API
├── 📄 04-instagram-comment.http # Instagram 댓글 API
├── 📄 05-instagram-insight.http # Instagram 인사이트 API
├── 📄 06-batch.http           # 배치 작업 API
└── 📄 README.md               # 이 파일
```

## 🔐 권한 체계

- **🔓 Public**: 인증 불필요
- **👤 User**: 일반 사용자 권한 필요
- **🔒 Admin**: 관리자 권한 필요

## 🚀 사용법

### 1. 환경 설정

각 HTTP 파일 상단에서 다음 변수들을 설정하세요:

```http
@authToken = your_jwt_token_here
@baseUrl = http://localhost:8000/api/v1/sns-service
```

### 2. HTTP 클라이언트 설치

다음 중 하나의 HTTP 클라이언트를 사용하세요:

- **IntelliJ IDEA**: HTTP Client 플러그인 (기본 내장)
- **VS Code**: REST Client 확장
- **Postman**: 별도 설치 필요

### 3. API 테스트

각 `.http` 파일을 열고 원하는 API 요청 위의 "Send Request" 버튼을 클릭하세요.

## 📱 API 그룹별 설명

### Instagram Auth API

- Instagram 계정 연결
- JWT 토큰 기반 인증

### Instagram Profile API

- 프로필 조회 및 관리
- 자동 삭제 설정
- Admin 권한으로 동기화

### Instagram Media API

- 미디어 조회 및 분석
- 인기 미디어 조회
- Admin 권한으로 동기화

### Instagram Comment API

- 댓글 검색 및 관리
- 금지어 관리
- 감정 분석 비율 조회
- 페이징 지원

### Instagram Insight API

- 인사이트 데이터 조회
- 팔로워 히스토리
- 참여도 분석
- Admin 권한으로 동기화

### Batch API

- 토큰 정리 배치
- Instagram 데이터 동기화
- 배치 상태 모니터링
- 비동기 실행

## 📝 응답 형식

모든 API는 다음과 같은 공통 응답 형식을 사용합니다:

```json
200 OK

{
  "code": "2000",
  "message": "OK",
  "message_code": null,
  "data": {
    ...
  }
}

```

## ⚠️ 주의사항

1. **JWT 토큰**: 모든 API 호출에 유효한 JWT 토큰이 필요합니다
2. **권한 확인**: Admin 권한이 필요한 API는 관리자 계정으로만 접근 가능합니다
3. **API 제한**: Instagram Graph API 호출 제한에 주의하세요
4. **배치 작업**: 중복 실행을 피하고 모니터링하세요

## 📚 추가 리소스

- [Instagram Graph API 문서](https://developers.facebook.com/docs/instagram-api/)
- [Clova Sentiment API 문서](https://developers.naver.com/docs/clova/api/sentiment/)
- [Spring Security 문서](https://spring.io/projects/spring-security)

