package authentication.two.service.token;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TokenMiddleware {      // Authentication Middleware
    
    @Autowired private JwtTokenizer tokenizer;

    public String createSessionToken(String username) {
        return tokenizer.generateToken(username);
    }

    public boolean validateSessionToken(String token) {
        return tokenizer.validateToken(token);
    }

    public String getSessionUsername(String token) {
        return tokenizer.extractUsername(token);
    }
}
