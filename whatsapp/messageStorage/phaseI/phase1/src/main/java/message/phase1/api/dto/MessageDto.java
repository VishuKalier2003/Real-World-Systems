package message.phase1.api.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Data transfer object class for <code>Messages</code> input */
@Getter
@Setter
@NoArgsConstructor
public class MessageDto {
    private Long conversationId;
    private Long senderId;
    private String message;
    private String type;        // content type: text, image, video, etc.
}
