package authentication.service.data;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class Register {
    private final String username;
    private final String email;
    private final String phone;
    private final String password;
}
