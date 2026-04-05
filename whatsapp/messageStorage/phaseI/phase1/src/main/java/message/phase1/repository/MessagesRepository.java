package message.phase1.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import message.phase1.model.Messages;

/** Interface to define Field Navigation <code>Messages</code> entity */
public interface MessagesRepository extends JpaRepository<Messages, Long> {
    Page<Messages> findByConversationConversationId(Long conversationId, Pageable pageable);
}
