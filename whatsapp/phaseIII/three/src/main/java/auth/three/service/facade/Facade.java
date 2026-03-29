package auth.three.service.facade;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import auth.three.api.dto.LoginDto;
import auth.three.api.dto.TokenDto;
import auth.three.api.dto.UserDto;
import auth.three.exceptions.RefreshTokenExpiredException;
import auth.three.exceptions.RefreshTokenNotFoundException;
import auth.three.model.Users;
import auth.three.service.routing.DatabaseRouting;
import auth.three.service.tokenization.TokenWrapper;
import auth.three.utils.dto.ExpirationOutput;

@Service
public class Facade {
    
    @Autowired private DatabaseRouting dbRouting;
    @Autowired @Qualifier("encryption") private PasswordEncoder encoder;
    @Autowired private TokenWrapper tokenWrapper;

    public boolean ifUserNotExists(UserDto dto) {
        return dbRouting.findByPhone(dto.getPhone()) == null && dbRouting.findByName(dto.getUsername()) == null;
    }

    public ExpirationOutput checkPassword(LoginDto dto) {
        Users user = dbRouting.findByName(dto.getUserID());
        if(encoder.matches(dto.getPassword(), user.getPassword())) {
            String refreshToken = tokenWrapper.createSession(dto.convertToTokenDto());
            return ExpirationOutput.builder().existTest(true).token(tokenWrapper.getJwtToken(dto.convertToTokenDto())).refreshToken(refreshToken).build();
        }
        return ExpirationOutput.builder().existTest(false).build();
    }

    public boolean save(UserDto dto) {
        return dbRouting.save(dto.convert(encoder.encode(dto.getPassword())));
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
        tokenWrapper.isValid(jwt);
        if(tokenWrapper.isExpired(jwt))
            return ExpirationOutput.builder().expired(true).token(tokenWrapper.refresh(jwt)).build();
        return ExpirationOutput.builder().expired(false).token(jwt).build();
    }
}
