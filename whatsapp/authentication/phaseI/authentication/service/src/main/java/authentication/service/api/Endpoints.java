package authentication.service.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import authentication.service.data.Register;
import authentication.service.service.Authenticate;
import authentication.service.service.Storage;

@RestController
@RequestMapping("/users")
public class Endpoints {
    
    @Autowired private Storage storage;
    @Autowired private Authenticate authenticate;

    @PostMapping("/register")
    public ResponseEntity<Object> register(@RequestBody Register data) {
        try {
            return ResponseEntity.status(HttpStatus.ACCEPTED).body(storage.register(
                data.getUsername(), data.getEmail(), data.getPhone(), data.getPassword()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getLocalizedMessage());
        }
    }

    @GetMapping("/token/{username}")
    public ResponseEntity<String> createSessionToken(@PathVariable String username) {
        try {
            if(!authenticate.authenticate(username))
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Username does not exist");
            return ResponseEntity.status(HttpStatus.ACCEPTED).body(authenticate.provideToken(username));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("");
        }
    }

    @PostMapping("/login")
    public ResponseEntity<Boolean> validate(@RequestParam String username, @RequestParam String token) {
        try {
            return ResponseEntity.status(HttpStatus.ACCEPTED).body(authenticate.validate(username, token));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(false);
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<Boolean> logout(@RequestParam String username) {
        try {
            return ResponseEntity.status(HttpStatus.ACCEPTED).body(authenticate.removeSession(username));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(false);
        }
    }
}
