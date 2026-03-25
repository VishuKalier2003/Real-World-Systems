package authentication.two.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import authentication.two.dto.Register;
import authentication.two.dto.Response;
import authentication.two.service.limiter.RateLimiter;
import authentication.two.service.register.RegisterStore;
import authentication.two.service.token.TokenMiddleware;

@RestController
@RequestMapping("/users")
public class Endpoints {
    
    @Autowired private RegisterStore store;
    @Autowired private RateLimiter limiter;
    @Autowired private TokenMiddleware middleware;

    @PostMapping("/signup")
    public ResponseEntity<Response> signup(@RequestBody Register dto) {
        Response response = Response.builder().build();
        try {
            if(!limiter.requestReceived(dto.getPhone())) {
                response = response.toBuilder().message("rate limiter blocked the request").flag(false).build();
                return ResponseEntity.status(HttpStatus.LOOP_DETECTED).body(response);
            }
            else if(store.checkifExists(dto)) {
                response = response.toBuilder().message("data parameters exist in store").flag(false).build();
                return ResponseEntity.status(HttpStatus.MULTI_STATUS).body(response);
            }
            else {
                response = response.toBuilder().message("user registered successfully").flag(store.register(dto)).build();
                return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
            }
        } catch (Exception e) {
            response = response.toBuilder().message("Exception : "+e.getLocalizedMessage()).flag(false).build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // Rate limiter blocking request for all cumulative as 3
    @PostMapping("/token/{username}/{phone}")
    public ResponseEntity<Response> getToken(@PathVariable String username, @PathVariable String phone) {
        Response response = Response.builder().build();
        try {
            if(!limiter.requestReceived(phone)) {
                response = response.toBuilder().message("rate limiter blocked the request").flag(false).build();
                return ResponseEntity.status(HttpStatus.LOOP_DETECTED).body(response);
            }
            else {
                response = response.toBuilder().message(middleware.createSessionToken(username)).flag(true).build();
                return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
            }
        } catch (Exception e) {
            response = response.toBuilder().message("Error : "+e.getLocalizedMessage()).flag(false).build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/login/{token}/{phone}")
    public ResponseEntity<Response> login(@PathVariable String token, @PathVariable String phone) {
        Response response = Response.builder().build();
        try {
            if(!limiter.requestReceived(phone)) {
                response = response.toBuilder().message("rate limiter blocked the request").flag(false).build();
                return ResponseEntity.status(HttpStatus.LOOP_DETECTED).body(response);
            }
            else {
                response = response.toBuilder().message("Status of token validation : "+middleware.validateSessionToken(token)).flag(true).build();
                return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
            }
        } catch (Exception e) {
            response = response.toBuilder().message("Error : "+e.getLocalizedMessage()).flag(false).build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
