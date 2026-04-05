package four.auth.service.redis;

import java.time.Duration;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import four.auth.utils.refresh.RefreshTokenData;
import io.lettuce.core.RedisException;

@Service
public class RedisSessionService {
    
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper mapper;

    public RedisSessionService(StringRedisTemplate template) {
        this.redisTemplate = template;
        this.mapper = new ObjectMapper();
        this.mapper.registerModule(new JavaTimeModule());
        this.mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    private String refreshKey(String key) {
        return "refresh:"+key;
    }

    private String userSessionKey(String userId) {
        return "user_sessions:" + userId;
    }

    public void storeRefreshToken(String token, RefreshTokenData data, long ttl) {
        try {
            String key = refreshKey(token);
            String value = mapper.writeValueAsString(data);
            redisTemplate.opsForValue().set(key, value, Duration.ofSeconds(ttl));
            // Defined internally like a map in Redis
            redisTemplate.opsForHash().put(
                // One key (userID) can have multiple fields (deviceID)
                userSessionKey(data.getUserID()), data.getDeviceID(), token
            );
            redisTemplate.expire(userSessionKey(data.getUserID()), Duration.ofSeconds(ttl));
        }
        catch(Exception e) {
            throw new RedisException("Error storing refresh token : "+e);
        }
    }

    public RefreshTokenData getRefreshTokenData(String token) {
        try {
            String data = redisTemplate.opsForValue().get(refreshKey(token));
            if(data == null)
                return null;
            return mapper.readValue(data, RefreshTokenData.class);
        }
        catch(Exception e) {
            throw new RedisException("Error reading refresh token : "+e);
        }
    }

    // The deletion in Redis is dependent on the keys
    public void deleteSession(String token, String userId, String deviceId) {
        // key directly available
        redisTemplate.delete(refreshKey(token));
        // key available by reaching the userID (internally map of map)
        redisTemplate.opsForHash().delete(userSessionKey(userId), deviceId);
    }

    public String getRefreshToken(String userId, String deviceId) {
        Object token = redisTemplate.opsForHash().get(userSessionKey(userId), deviceId);
        return token != null ? token.toString() : null;
    }

    public boolean sessionExists(String userId, String deviceId) {
        try {
            return redisTemplate.opsForHash()
                    .hasKey(userSessionKey(userId), deviceId);
        } catch (Exception e) {
            throw new RedisException("Error checking session existence: " + e);
        }
    }

    public void blacklistToken(String token, long ttl) {
        redisTemplate.opsForValue().set("blacklist:" + token, "true", Duration.ofSeconds(ttl));
    }

    public boolean isBlacklisted(String token) {
        return redisTemplate.hasKey("blacklist:" + token);
    }

    public long incrementLoginAttempts(String userId) {
        String key = "login_attempts:" + userId;
        Long count = redisTemplate.opsForValue().increment(key);
        if (count == 1) {
            redisTemplate.expire(key, Duration.ofMinutes(5));
        }
        return count;
    }

    public boolean isRateLimited(String userId, long maxAttempts) {
        String key = "login_attempts:" + userId;
        String val = redisTemplate.opsForValue().get(key);
        if (val == null) return false;
        return Long.parseLong(val) >= maxAttempts;
    }
}
