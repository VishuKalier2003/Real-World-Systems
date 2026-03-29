package four.auth.api.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class LoginDto {
    private String userID;
    private String phone;
    private String password;
    private String deviceID;

    public UserDto convert() {
        UserDto dto = new UserDto();
        dto.setUsername(userID);
        dto.setPhone(phone);
        dto.setPassword(password);
        return dto;
    }

    public TokenDto convertToTokenDto() {
        return new TokenDto(userID, deviceID);
    }
}
