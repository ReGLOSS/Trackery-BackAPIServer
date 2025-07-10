# JWT RefreshToken Null 문제 해결 보고서

## 📋 **문제 개요**

### 발생 현상
- Redis에 저장된 RefreshToken 값이 null로 조회되는 문제
- 액세스 토큰 만료 시 리프레시 토큰으로 재발급 과정에서 실패
- Jackson JSON 파싱 에러 발생: `JsonParseException: Illegal character ((CTRL-CHAR, code 0))`

### 발생 시점
- 액세스 토큰 강제 만료 후 자동 토큰 재발급 시도 시
- 사용자 로그인 후 일정 시간 경과 후 API 요청 시

---

## 🔍 **원인 분석**

### 1차 원인: Redis 데이터 손상
- **NULL 문자(`\0`) 포함**: JSON 문자열에 제어 문자가 섞여 들어감
- **데이터 무결성 문제**: Redis 저장 과정에서 바이너리 데이터 오염
- **인코딩 문제**: UTF-8이 아닌 다른 인코딩으로 저장/조회

### 2차 원인: 예외 처리 부족
- **저장 후 검증 없음**: Redis에 저장 후 실제 저장 여부 확인 안 함
- **JSON 유효성 검사 부재**: 파싱 전 데이터 유효성 검증 없음
- **에러 처리 미흡**: 구체적인 에러 상황별 대응 부족

### 3차 원인: 디버깅 정보 부족
- **로깅 부족**: 저장/조회 과정에서 상세한 로그 없음
- **상태 추적 어려움**: 문제 발생 지점 파악 불가

---

## 🛠️ **해결 방안**

### A. JwtRedisService 개선

#### 1. 입력 검증 강화
```java
// 개선 전
public void saveRefreshToken(RefreshTokenDto refreshTokenDto) {
    try {
        String redisKey = REFRESH_TOKEN_REDIS_KEY + refreshTokenDto.refreshToken();
        // 바로 사용 - null 체크 없음
```

```java
// 개선 후
public void saveRefreshToken(RefreshTokenDto refreshTokenDto) {
    // 입력 검증 추가
    if (refreshTokenDto == null || refreshTokenDto.refreshToken() == null) {
        log.error("RefreshTokenDto or refreshToken is null");
        throw new ApiException(ErrorCode.BAD_REQUEST);
    }
```

#### 2. 저장 후 검증 로직 추가
```java
// 개선 전
redisTemplate.opsForValue().set(redisKey, jsonRefreshTokenDto, ttl);
// 저장만 하고 끝
```

```java
// 개선 후
redisTemplate.opsForValue().set(redisKey, jsonRefreshTokenDto, ttl);

// 저장 후 실제로 저장되었는지 확인
String savedValue = redisTemplate.opsForValue().get(redisKey);
if (savedValue == null) {
    log.error("Failed to save refresh token to Redis");
    throw new ApiException(ErrorCode.INTERNAL_SERVER_ERROR);
}
```

#### 3. 데이터 무결성 검증
```java
// 제어 문자 및 데이터 무결성 검증 추가
if (jsonRefreshTokenDto.contains("\0") || !isValidJson(jsonRefreshTokenDto)) {
    log.error("Corrupted refresh token data detected");
    redisTemplate.delete(redisKey); // 손상된 데이터 정리
    throw new ApiException(ErrorCode.UNAUTHORIZED);
}
```

#### 4. JSON 유효성 검사 헬퍼 메서드
```java
private boolean isValidJson(String jsonString) {
    try {
        objectMapper.readTree(jsonString);
        return true;
    } catch (Exception e) {
        return false;
    }
}
```

### B. JwtResolverFilter 개선

#### 1. 상세한 로깅 추가
```java
// 개선 전
Long userId = jwtService.parseAndVerifyRefreshToken(refreshToken);
```

```java
// 개선 후
try {
    log.info("Attempting to reissue access token using refresh token");
    Long userId = jwtService.parseAndVerifyRefreshToken(refreshToken);
    log.info("Successfully parsed refresh token for userId: {}", userId);
} catch (ApiException e) {
    log.error("Failed to reissue access token. ErrorCode: {}, Message: {}", 
        e.getErrorCode(), e.getMessage());
}
```

#### 2. 손상된 쿠키 정리
```java
// 손상된 리프레시 토큰 쿠키 정리
if (e.getErrorCode() == ErrorCode.UNAUTHORIZED || 
    e.getErrorCode() == ErrorCode.INTERNAL_SERVER_ERROR) {
    clearRefreshTokenCookie(response);
}
```

### C. 관리용 메서드 추가

#### 1. Redis 상태 체크
```java
public void checkRedisHealth() {
    try {
        redisTemplate.opsForValue().set("health_check", "ok", Duration.ofSeconds(10));
        String result = redisTemplate.opsForValue().get("health_check");
        log.info("Redis health check result: {}", result);
    } catch (Exception e) {
        log.error("Redis health check failed", e);
    }
}
```

#### 2. 손상된 토큰 일괄 정리
```java
public void cleanupCorruptedTokens() {
    Set<String> keys = redisTemplate.keys(REFRESH_TOKEN_REDIS_KEY + "*");
    int cleanedCount = 0;
    for (String key : keys) {
        String value = redisTemplate.opsForValue().get(key);
        if (value != null && (value.contains("\0") || !isValidJson(value))) {
            redisTemplate.delete(key);
            cleanedCount++;
        }
    }
    log.info("Cleanup completed. Removed {} corrupted tokens", cleanedCount);
}
```

---

## 📊 **해결 결과**

### 개선 효과
1. **문제 조기 발견**: 저장 후 즉시 검증으로 문제 조기 감지
2. **자동 정리**: 손상된 데이터 자동 탐지 및 삭제
3. **디버깅 향상**: 상세한 로그로 문제 지점 정확히 파악
4. **사용자 경험 개선**: 손상된 쿠키 자동 정리로 재로그인 유도

### 추가된 보안 기능
- NULL 문자 탐지 및 처리
- JSON 유효성 실시간 검사
- Redis 정합성 문제 대응
- 손상된 데이터 자동 정리

---

## 🔧 **운영 가이드**

### 문제 발생 시 확인 사항
1. **Redis 로그 확인**
   ```bash
   # Redis 연결 상태 확인
   redis-cli ping
   
   # 메모리 사용량 확인
   redis-cli info memory
   
   # 실제 저장된 키 확인
   redis-cli keys "jwtRefreshToken:*"
   ```

2. **손상된 토큰 정리**
   ```java
   // 애플리케이션에서 실행
   jwtRedisService.cleanupCorruptedTokens();
   ```

3. **Redis 상태 체크**
   ```java
   jwtRedisService.checkRedisHealth();
   ```

### 예방 조치
1. **Redis 모니터링**: 메모리 사용량 및 연결 상태 정기 점검
2. **로그 모니터링**: 토큰 관련 에러 로그 정기 확인
3. **정기 정리**: 손상된 토큰 정리 배치 작업 고려

---

## 📈 **향후 개선 계획**

### 단기 계획
- [ ] Redis Sentinel 구성으로 고가용성 확보
- [ ] 토큰 관련 메트릭 수집 및 모니터링
- [ ] Circuit Breaker 패턴 적용

### 중기 계획
- [ ] Redis Cluster 구성 검토
- [ ] 토큰 암호화 저장 방식 도입
- [ ] Bloom Filter를 활용한 블랙리스트 성능 개선

---

## 📝 **관련 파일**

### 수정된 파일
- `JwtRedisService.java` - Redis 작업 안정성 강화
- `JwtResolverFilter.java` - 에러 처리 및 로깅 개선

### 추가된 기능
- JSON 유효성 검사 메서드
- Redis 상태 체크 메서드
- 손상된 토큰 정리 메서드
- 쿠키 정리 메서드

---

**작성일**: 2025-07-08  
**작성자**: Claude Code Assistant  
**문서 버전**: 1.0