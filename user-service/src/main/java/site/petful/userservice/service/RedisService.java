package site.petful.userservice.service;

public interface RedisService {
    void setValue(String key, String value, long timeoutSeconds);
    String getValue(String key);
    void deleteValue(String key);
    boolean exists(String key);
    boolean testConnection();
}
