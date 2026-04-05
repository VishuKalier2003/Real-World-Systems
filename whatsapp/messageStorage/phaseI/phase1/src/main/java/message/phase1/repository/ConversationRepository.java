package message.phase1.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import message.phase1.model.Conversation;

/** Interface to define Field Navigation for <code>Conversation</code> entity */
public interface ConversationRepository extends JpaRepository<Conversation, Long> {
    
}
