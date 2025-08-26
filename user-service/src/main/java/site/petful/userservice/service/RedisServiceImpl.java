package site.petful.userservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class RedisServiceImpl implements RedisService {
    
    private final StringRedisTemplate redisTemplate;
    
    @Override
    public void setValue(String key, String value, long timeoutSeconds) {
        redisTemplate.opsForValue().set(key, value, Duration.ofSeconds(timeoutSeconds));
    }
    
    @Override
    public String getValue(String key) {
        return redisTemplate.opsForValue().get(key);
    }
    
    @Override
    public void deleteValue(String key) {
        redisTemplate.delete(key);
    }
    
    @Override
    public boolean exists(String key) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }
    
    @Override
    public boolean testConnection() {
        try {
            redisTemplate.opsForValue().set("test_connection", "test", Duration.ofSeconds(10));
            String result = redisTemplate.opsForValue().get("test_connection");
            return "test".equals(result);
        } catch (Exception e) {
            System.err.println("Redis 연결 테스트 실패: " + e.getMessage());
            return false;
        }
    }
}
