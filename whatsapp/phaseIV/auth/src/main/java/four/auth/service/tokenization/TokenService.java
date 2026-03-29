package four.auth.service.tokenization;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Date;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import four.auth.config.RsaKeyProvider;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;

@Service
public class TokenService {
    
    private final PrivateKey privateKey;
    private final PublicKey publicKey;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    public TokenService(RsaKeyProvider provider) {
        this.privateKey = provider.getPrivateKey();
        this.publicKey = provider.getPublicKey();
    }

    public String generateAccessToken(String userID, String deviceID) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + jwtExpiration);
        return Jwts.builder()
                .subject(userID)
                .claim("deviceId", deviceID)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(privateKey, Jwts.SIG.RS256)
                .compact();
    }

    public Claims validateAndGetClaims(String token) {
        return Jwts.parser()
                .verifyWith(publicKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String extractUserId(String token) {
        return validateAndGetClaims(token).getSubject();
    }

    public String extractDeviceId(String token) {
        return validateAndGetClaims(token).get("deviceId", String.class);
    }

    public boolean isTokenExpired(String token) {
        Date expiry = validateAndGetClaims(token).getExpiration();
        return expiry.before(new Date());
    }

    public boolean isValid(String token) {
        try {
            validateAndGetClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
