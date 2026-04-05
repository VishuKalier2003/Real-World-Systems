package four.auth.api.endpoints;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import four.auth.api.dto.DataDto;
import four.auth.api.dto.LoginDto;
import four.auth.api.dto.TokenDto;
import four.auth.api.dto.UserDto;
import four.auth.service.facade.Facade;
import four.auth.utils.dto.ExpirationOutput;

@RestController
@RequestMapping("/p3")
public class Endpoint {
    @Autowired private Facade facade;

    @PostMapping("/signup")
    public ResponseEntity<String> signup(@RequestBody UserDto dto) {
        if(facade.ifUserNotExists(dto))
            return ResponseEntity.status(HttpStatus.ACCEPTED).body("User register status : "+facade.save(dto));
        return ResponseEntity.status(HttpStatus.CONFLICT).body("User already exists");
    }

    @PostMapping("/token")
    public ResponseEntity<Object> token(@RequestBody DataDto dto) {
        TokenDto tknDto = new TokenDto(dto.getUserID(), dto.getDeviceID());
        facade.checkSession(tknDto);  // throws if invalid
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(facade.checkJwt(dto.getToken()));
    }

    @PostMapping("/login")
    public ResponseEntity<Object> login(@RequestBody LoginDto dto) {
        if(!facade.ifUserNotExists(dto.convert())) {
            ExpirationOutput output = facade.checkPassword(dto);
            if(output.isExistTest())
                return ResponseEntity.status(HttpStatus.ACCEPTED).body(output);
            else
                return ResponseEntity.status(HttpStatus.NO_CONTENT).body("Login unsuccessful");
        }
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body("No user for the given data exists");
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(@RequestBody DataDto dto) {
        facade.logout(new TokenDto(dto.getUserID(), dto.getDeviceID()), dto.getToken());
        return ResponseEntity.ok("Logged out successfully");
    }
}
