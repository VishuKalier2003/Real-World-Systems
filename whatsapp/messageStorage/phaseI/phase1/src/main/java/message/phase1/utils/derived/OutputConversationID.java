package message.phase1.utils.derived;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/** Subclass of <code>Output</code> storing conversationId */
@Getter
@Setter
@SuperBuilder
public class OutputConversationID extends Output {
    private Long conversationId;
}
