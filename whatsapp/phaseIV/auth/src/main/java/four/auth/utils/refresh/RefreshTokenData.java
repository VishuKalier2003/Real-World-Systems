package four.auth.utils.refresh;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class RefreshTokenData {
    private String userID;
    private String deviceID;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Instant expiresAt;
}
