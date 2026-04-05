package authentication.service.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class Encryption {
    @Autowired @Qualifier("encryptor") private PasswordEncoder encoder;

    public String encryptPassword(String password) {
        return encoder.encode(password);
    }

    public boolean matches(String raw, String hashed) {
        return encoder.matches(raw, hashed);
    }
}
