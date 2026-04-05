package message.phase1.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import message.phase1.model.ConversationParticipant;
import message.phase1.model.keys.ConversationParticipantKey;

/** Interface to define field navigation and query projection for <code>ConversationParticipant</code> entity */
public interface ConversationParticipantRepository extends JpaRepository<ConversationParticipant, ConversationParticipantKey> {
    // Field Navigation
    List<ConversationParticipant> findByUserUserId(Long userId);
    List<ConversationParticipant> findByConversationConversationId(Long conversationId);

    // Query projection
    @Query("""
    SELECT cp.user.userId
    FROM ConversationParticipant cp
    WHERE cp.conversation.conversationId = :conversationId
    """)
    List<Long> findUserIdsByConversationId(Long conversationId);

    @Query("""
    SELECT cp.conversation.conversationId
    FROM ConversationParticipant cp
    WHERE cp.user.userId = :userId
    """)
    List<Long> findConversationIdsByUserId(Long userId);
}
