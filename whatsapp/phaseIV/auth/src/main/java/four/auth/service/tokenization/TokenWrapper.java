package four.auth.service.tokenization;

import java.time.Instant;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import four.auth.api.dto.TokenDto;
import four.auth.service.redis.RedisSessionService;
import four.auth.utils.refresh.RefreshTokenData;

@Service
public class TokenWrapper {
    
    private final RefreshTokenizer tokenizer;
    private final TokenService service;
    private final RedisSessionService redis;
    private final long TTL;

    public TokenWrapper(
        RefreshTokenizer tokenizer,
        TokenService service,
        RedisSessionService redis,
        @Value("${ttl.expiration}") Long ttl
    ) {
        this.tokenizer = tokenizer;
        this.service = service;
        this.redis = redis;
        this.TTL = ttl;
    }

    public String createSession(TokenDto dto) {
        String existingToken = redis.getRefreshToken(dto.getUserID(), dto.getDeviceID());
        if (existingToken != null) {
            redis.deleteSession(existingToken, dto.getUserID(), dto.getDeviceID());
        }
    String refreshToken = tokenizer.generateRefreshToken();
        redis.storeRefreshToken(
            refreshToken,
            RefreshTokenData.builder()
                .userID(dto.getUserID())
                .deviceID(dto.getDeviceID())
                .expiresAt(Instant.now().plusSeconds(TTL))
                .build(),
            TTL
        );
        return refreshToken;
    }

    public boolean isRefreshTokenExpired(TokenDto dto) {
        String token = redis.getRefreshToken(dto.getUserID(), dto.getDeviceID());
        if (token == null) return true;  // treat missing session as expired
        RefreshTokenData data = redis.getRefreshTokenData(token);
        if (data == null) return true;
        return data.getExpiresAt().isBefore(Instant.now());
    }

    public void deleteSession(TokenDto dto) {
        redis.deleteSession(redis.getRefreshToken(dto.getUserID(), dto.getDeviceID()), dto.getUserID(), dto.getDeviceID());
    }

    public boolean sessionExist(TokenDto dto) {
        return redis.sessionExists(dto.getUserID(), dto.getDeviceID());
    }

    public String getJwtToken(TokenDto dto) {
        return service.generateAccessToken(dto.getUserID(), dto.getDeviceID());
    }

    public boolean isValid(String token) {
        if (redis.isBlacklisted(token)) {
            return false;
        }
        return service.isValid(token);
    }

    public boolean isExpired(String token) {
        return service.isTokenExpired(token);
    }

    public String refresh(String token) {
        String userID = service.extractUserId(token);
        String deviceID = service.extractDeviceId(token);
        return getJwtToken(new TokenDto(userID, deviceID));
    }

    public String getRefreshToken(TokenDto dto) {
        return redis.getRefreshToken(dto.getUserID(), dto.getDeviceID());
    }

    public void logout(TokenDto dto, String jwt) {
        redis.deleteSession(
            redis.getRefreshToken(dto.getUserID(), dto.getDeviceID()),
            dto.getUserID(),
            dto.getDeviceID()
        );
        redis.blacklistToken(jwt, TTL);
    }

    public boolean isBlacklisted(String token) {
        return redis.isBlacklisted(token);
    }

    public void blacklist(String token) {
        long ttl = service.validateAndGetClaims(token)
                        .getExpiration()
                        .toInstant()
                        .getEpochSecond() - Instant.now().getEpochSecond();
        if (ttl > 0) {
            redis.blacklistToken(token, ttl);
        }
    }

    public boolean isRateLimited(String userId, int maxAttempts) {
        return redis.isRateLimited(userId, maxAttempts);
    }

    public void incrementAttempts(String userId) {
        redis.incrementLoginAttempts(userId);
    }
}
