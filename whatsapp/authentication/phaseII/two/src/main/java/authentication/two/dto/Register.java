package authentication.two.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Register {
    private String username;
    private String phone;
    private String email;
    private String password;
}
