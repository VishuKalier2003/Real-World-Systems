package auth.three.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class EncryptionConfig {
    
    @Bean(name="encryption")
    public PasswordEncoder encryption() {
        return new BCryptPasswordEncoder(4);
    }
}
