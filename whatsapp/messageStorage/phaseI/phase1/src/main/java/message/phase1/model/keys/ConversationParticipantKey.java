package message.phase1.model.keys;

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;

/** Composite Key defined via Jakarta for <code>ConversationParticipant</code> entity implementing <code>Serializable</code> */
@Getter
@Setter
public class ConversationParticipantKey implements Serializable {
    private Long conversation;
    private Long user;

    @Override
    public boolean equals(Object o) {
        if(this == o)
            return true;
        if(o == null || getClass() != o.getClass())
            return false;
        ConversationParticipantKey that = (ConversationParticipantKey) o;
        return conversation.equals(that.conversation) && user.equals(that.user);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(conversation, user);
    }
}
