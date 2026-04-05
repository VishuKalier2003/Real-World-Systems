package message.phase1.utils.derived;

import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import message.phase1.model.Messages;

/** Subclass of <code>Output</code> storing messageList */
@Getter
@Setter
@SuperBuilder
public class OutputConversationsID extends Output {
    private List<Messages> messageList;
}
