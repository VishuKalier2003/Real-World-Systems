package auth.three.service.tokenization;

import org.springframework.stereotype.Service;

import java.security.SecureRandom;

@Service
public class RefreshTokenizer {
    private static final String LETTERS = "AaBbCcDdEeFfGgHhIiJjKkLlMmNnOoPpQqRrSsTtUuVvWwXxYyZz1234567890";
    private static final SecureRandom RANDOM = new SecureRandom();

    public String generateRefreshToken() {
        StringBuilder builder = new StringBuilder(32);
        int n = LETTERS.length();
        for (int i = 0; i < 32; i++)   // also increased to 32 chars
            builder.append(LETTERS.charAt(RANDOM.nextInt(n)));
        return builder.toString();
    }
}
