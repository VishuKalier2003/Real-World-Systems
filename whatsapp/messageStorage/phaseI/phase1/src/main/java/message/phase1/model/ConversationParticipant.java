package message.phase1.model;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import message.phase1.model.keys.ConversationParticipantKey;

/** Entity mapping with Primary Key of <code>Conversation</code> and <code>Users</code> and Composite Key of <code>ConversationParticipantKey</code> entity for storing conversation and user Id as per 1NF */
@Getter
@Setter
@Entity
@Table(name = "conversation_participant")
@NoArgsConstructor
public class ConversationParticipant {
    
    @EmbeddedId
    private ConversationParticipantKey id;

    @ManyToOne
    @MapsId("conversation")     // Maps the Embeddedid (Composite Key)
    @JoinColumn(name = "conversation_id", nullable = false)
    private Conversation conversation;

    @ManyToOne
    @MapsId("user")
    @JoinColumn(name = "user_id", nullable = false)
    private Users user;
}
