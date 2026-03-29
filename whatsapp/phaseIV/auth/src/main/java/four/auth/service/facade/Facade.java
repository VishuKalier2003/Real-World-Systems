package four.auth.service.facade;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import four.auth.api.dto.LoginDto;
import four.auth.api.dto.TokenDto;
import four.auth.api.dto.UserDto;
import four.auth.exceptions.NoUserFoundException;
import four.auth.exceptions.RefreshTokenExpiredException;
import four.auth.exceptions.RefreshTokenNotFoundException;
import four.auth.model.Users;
import four.auth.repo.UserAuthRepo;
import four.auth.service.tokenization.TokenWrapper;
import four.auth.utils.dto.ExpirationOutput;

@Service
public class Facade {
    
    @Autowired private UserAuthRepo repo;
    @Autowired @Qualifier("encryption") private PasswordEncoder encoder;
    @Autowired private TokenWrapper tokenWrapper;

    public boolean ifUserNotExists(UserDto dto) {
        return repo.findByPhone(dto.getPhone()).isEmpty() && repo.findByName(dto.getUsername()).isEmpty();
    }

    public ExpirationOutput checkPassword(LoginDto dto) {
        if (tokenWrapper.isRateLimited(dto.getUserID(), 5)) {
            throw new RuntimeException("Too many login attempts. Try later.");
        }
        Users user = repo.findByName(dto.getUserID()).orElse(null);
        if(user == null)
            throw new NoUserFoundException();
        if(encoder.matches(dto.getPassword(), user.getPassword())) {
            String refreshToken = tokenWrapper.createSession(dto.convertToTokenDto());
            return ExpirationOutput.builder().existTest(true).token(tokenWrapper.getJwtToken(dto.convertToTokenDto())).refreshToken(refreshToken).build();
        }
        tokenWrapper.incrementAttempts(dto.getUserID());
        return ExpirationOutput.builder().existTest(false).build();
    }

    public boolean save(UserDto dto) {
        return repo.save(dto.convert(encoder.encode(dto.getPassword()))) != null;
    }

    public String checkSession(TokenDto dto) {
        if(!tokenWrapper.sessionExist(dto))
            throw new RefreshTokenNotFoundException();
        else if(tokenWrapper.isRefreshTokenExpired(dto)) {
            tokenWrapper.deleteSession(dto);
            throw new RefreshTokenExpiredException();
        }
        return tokenWrapper.getJwtToken(dto);
    }

    public ExpirationOutput checkJwt(String jwt) {
        if (!tokenWrapper.isValid(jwt)) {
            throw new RuntimeException("Invalid or blacklisted token");
        }
        if (tokenWrapper.isExpired(jwt)) {
            return ExpirationOutput.builder()
                .expired(true)
                .token(tokenWrapper.refresh(jwt))
                .build();
        }
        return ExpirationOutput.builder()
                .expired(false)
                .token(jwt)
                .build();
    }

    public void logout(TokenDto dto, String jwtToken) {
        // Delete refresh session
        tokenWrapper.deleteSession(dto);
        // Blacklist access token
        tokenWrapper.blacklist(jwtToken);
    }
}
