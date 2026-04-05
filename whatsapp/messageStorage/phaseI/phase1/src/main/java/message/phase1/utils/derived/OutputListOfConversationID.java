package message.phase1.utils.derived;

import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/** Subclass of <code>Output</code> storing list of conversationIds */
@Getter
@Setter
@SuperBuilder
public class OutputListOfConversationID extends Output {
    
    private List<Long> conversationIDs;
}
