# Instagram 배치 작업 가이드

## 개요

이 프로젝트는 스프링 배치를 사용하여 인스타그램 데이터를 자동으로 동기화하는 배치 작업을 제공합니다.

## 배치 작업 구성

### 1. 프로필 동기화 (Profile Sync)
- 인스타그램 비즈니스 계정 프로필 정보 동기화
- 팔로워 수, 팔로잉 수, 미디어 수 등 기본 정보 업데이트

### 2. 미디어 데이터 동기화 (Media Sync)
- 인스타그램 게시물 정보 동기화
- 게시물 ID, URL, 타임스탬프 등 메타데이터 수집

### 3. 댓글 동기화 (Comment Sync)
- 각 게시물의 댓글 데이터 동기화
- 병렬 처리로 성능 최적화

### 4. 인사이트 동기화 (Insight Sync)
- **스케줄러 실행**: 매일 새벽 2시 자동 실행, 최근 1개월 데이터만 동기화
- **수동 API 실행**: 사용자 요청 시 실행, 기본값 6개월 데이터 동기화
- **개월수 지정 가능**: 배치 실행 시 파라미터로 동기화할 개월 수 지정 가능 (1개월~12개월)
- **비동기 처리**: 모든 수동 실행은 비동기로 처리되어 즉시 응답 반환

## 스케줄링

- **자동 실행**: 매일 새벽 2시 (`0 0 2 * * *`)
- **수동 실행**: REST API를 통한 즉시 실행 가능
- **비동기 처리**: 모든 수동 실행은 비동기로 처리되어 즉시 응답 반환

## 비동기 처리

### 특징
- **즉시 응답**: API 호출 시 배치 작업이 백그라운드에서 실행되며 즉시 응답 반환
- **스레드 풀**: 전용 스레드 풀을 사용하여 배치 작업 처리
- **에러 핸들링**: 비동기 실행 중 발생하는 오류는 로그로 기록
- **모니터링**: 배치 작업 완료 시 로그로 결과 확인 가능

### 스레드 풀 설정
- **기본 스레드 풀**: 5개
- **최대 스레드 풀**: 10개
- **큐 용량**: 25개
- **스레드 이름**: InstagramBatch-{번호}

## API 엔드포인트

### 1. 전체 Instagram 동기화 배치 실행
```http
POST /api/v1/batch/instagram/sync
```

**응답 예시:**
```json
{
  "success": true,
  "data": {
    "status": "success",
    "message": "Instagram 동기화 배치 작업이 비동기로 시작되었습니다. (기본값: 6개월)",
    "executionTime": "2024-01-15 14:30:00",
    "executionType": "manual_async",
    "monthsToSync": 6
  }
}
```

### 2. 특정 사용자 Instagram 동기화 배치 실행
```http
POST /api/v1/batch/instagram/sync/user/{userNo}
```

**응답 예시:**
```json
{
  "success": true,
  "data": {
    "status": "success",
    "message": "사용자 1에 대한 Instagram 동기화 배치 작업이 비동기로 시작되었습니다. (기본값: 6개월)",
    "executionTime": "2024-01-15 14:30:00",
    "executionType": "manual_async",
    "targetUserNo": 1,
    "monthsToSync": 6
  }
}
```

### 3. 개월수 지정 Instagram 동기화 배치 실행
```http
POST /api/v1/batch/instagram/sync/months/{months}
```

**파라미터:**
- `months`: 동기화할 개월 수 (예: 3, 6, 12)

**응답 예시:**
```json
{
  "success": true,
  "data": {
    "status": "success",
    "message": "3개월 데이터 동기화를 위한 Instagram 동기화 배치 작업이 비동기로 시작되었습니다.",
    "executionTime": "2024-01-15 14:30:00",
    "executionType": "manual_async",
    "monthsToSync": 3
  }
}
```

### 4. 특정 사용자와 개월수 지정 Instagram 동기화 배치 실행
```http
POST /api/v1/batch/instagram/sync/user/{userNo}/months/{months}
```

**파라미터:**
- `userNo`: 대상 사용자 번호
- `months`: 동기화할 개월 수

**응답 예시:**
```json
{
  "success": true,
  "data": {
    "status": "success",
    "message": "사용자 1에 대한 6개월 데이터 동기화를 위한 Instagram 동기화 배치 작업이 비동기로 시작되었습니다.",
    "executionTime": "2024-01-15 14:30:00",
    "executionType": "manual_async",
    "targetUserNo": 1,
    "monthsToSync": 6
  }
}
```

### 5. 배치 작업 상태 확인
```http
GET /api/v1/batch/instagram/status
```

**응답 예시:**
```json
{
  "success": true,
  "data": {
    "batchName": "Instagram 동기화 배치",
    "description": "인스타그램 프로필, 미디어, 댓글, 인사이트 데이터를 동기화하는 배치 작업",
    "schedule": "매일 새벽 2시",
    "lastExecution": "2024-01-15 14:30:00",
    "status": "active"
  }
}
```

## 설정

### application.yml
```yaml
spring:
  batch:
    job:
      enabled: false  # 애플리케이션 시작 시 Job 자동 실행 방지
    jdbc:
      initialize-schema: always  # 배치 메타데이터 테이블 자동 생성
```

### 로깅 레벨
```yaml
logging:
  level:
    org.springframework.batch: DEBUG
    site.petful.snsservice.batch: DEBUG
```

## 배치 작업 실행 흐름

```
Instagram 배치 작업 시작
    ↓
1. 프로필 동기화 단계
    ↓
2. 미디어 데이터 동기화 단계
    ↓
3. 댓글 동기화 단계
    ↓
4. 인사이트 동기화 단계
    ↓
Instagram 배치 작업 완료
```

## 모니터링

### 로그 확인
배치 작업 실행 시 다음과 같은 로그를 확인할 수 있습니다:

#### 스케줄러 실행 (동기)
```
=== Instagram 배치 작업 시작 ===
작업 ID: 1
작업 이름: instagramSyncJob
시작 시간: 2024-01-15 02:00:00

=== Instagram 배치 단계 시작 ===
단계 이름: profileSyncStep
Instagram 프로필 동기화 시작
프로필 동기화 대상 사용자 수: 1
사용자 1 프로필 동기화 완료
Instagram 프로필 동기화 완료

=== Instagram 배치 단계 완료 ===
단계 이름: profileSyncStep
단계 실행 시간: 5000ms (5초)

... (다른 단계들)

=== Instagram 배치 작업 완료 ===
작업 ID: 1
작업 이름: instagramSyncJob
총 실행 시간: 25000ms (25초)
배치 작업 성공적으로 완료되었습니다.
```

#### 수동 실행 (비동기)
```
=== Instagram 배치 작업 비동기 시작 ===
Instagram 동기화 배치 작업이 비동기로 시작되었습니다. (기본값: 6개월)
Instagram 동기화 배치 작업 비동기 실행 완료

=== 배치 작업 백그라운드 실행 ===
Instagram 인사이트 동기화 시작
동기화할 개월 수: 6개월 (실행 타입: 수동)
인사이트 동기화 대상 사용자 수: 1
프로필 123 인사이트 동기화 완료 (6개월)
사용자 1 인사이트 동기화 완료
Instagram 인사이트 동기화 완료
```

### 비동기 처리 모니터링
- **즉시 응답**: API 호출 시 배치 작업 시작 확인
- **백그라운드 실행**: 배치 작업이 별도 스레드에서 실행
- **완료 알림**: 배치 작업 완료 시 로그로 결과 확인
- **에러 추적**: 실행 중 오류 발생 시 로그로 기록

## 주의사항

1. **토큰 관리**: Instagram API 액세스 토큰이 유효한지 확인
2. **API 제한**: Instagram API 호출 제한을 고려하여 배치 작업 설계
3. **에러 처리**: 개별 사용자 오류가 전체 배치를 중단시키지 않도록 설계
4. **데이터베이스**: 배치 메타데이터를 저장할 데이터베이스 연결 필요

## 사용 예시

### 개월수 지정 동기화

#### 3개월 데이터 동기화
```bash
curl -X POST http://localhost:8080/api/v1/batch/instagram/sync/months/3
```

#### 특정 사용자의 6개월 데이터 동기화
```bash
curl -X POST http://localhost:8080/api/v1/batch/instagram/sync/user/1/months/6
```

#### 전체 사용자의 12개월 데이터 동기화
```bash
curl -X POST http://localhost:8080/api/v1/batch/instagram/sync/months/12
```

### 개월수별 동기화 데이터 범위

- **1개월**: 스케줄러 자동 실행 시 기본값 (매일 새벽 2시)
- **6개월**: 수동 API 실행 시 기본값 (사용자 요청 시)
- **3개월**: 사용자 지정 (분기별 분석에 유용)
- **12개월**: 사용자 지정 (연간 분석에 유용)

**주의**: 
- 스케줄러는 매일 1개월 데이터만 동기화하여 API 호출을 최소화
- 수동 실행 시에는 더 많은 기간 데이터를 동기화할 수 있음
- 개월수가 많을수록 API 호출 횟수가 증가하므로 Instagram API 제한을 고려해야 함

## 문제 해결

### 배치 작업이 실행되지 않는 경우
1. `spring.batch.job.enabled=false` 설정 확인
2. 데이터베이스 연결 상태 확인
3. Instagram API 토큰 유효성 확인

### 배치 작업이 실패하는 경우
1. 로그에서 오류 메시지 확인
2. Instagram API 응답 상태 확인
3. 데이터베이스 연결 및 권한 확인

## 개발 환경

- Java 17
- Spring Boot 3.5.4
- Spring Batch
- MySQL 8.0+
- Gradle
