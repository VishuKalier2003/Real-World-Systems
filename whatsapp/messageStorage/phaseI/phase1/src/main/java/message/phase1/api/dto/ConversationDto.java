package message.phase1.api.dto;

import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Data transfer object class for <code>Conversation</code> input */
@Getter
@Setter
@NoArgsConstructor
public class ConversationDto {
    private String type;
    private List<Long> participants;
}
