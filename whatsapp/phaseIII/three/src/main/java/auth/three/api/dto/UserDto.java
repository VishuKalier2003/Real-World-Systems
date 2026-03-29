package auth.three.api.dto;

import auth.three.model.Users;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserDto {
    private String username;
    private String phone;
    private String password;

    public Users convert(String hashPassword) {
        Users users = new Users();
        users.setName(username);
        users.setPhone(phone);
        users.setPassword(hashPassword);
        return users;
    }
}
