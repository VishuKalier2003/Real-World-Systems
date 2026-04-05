package message.phase1.service.repo;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.persistence.EntityManager;
import message.phase1.model.Conversation;
import message.phase1.model.ConversationParticipant;
import message.phase1.model.Users;
import message.phase1.model.keys.ConversationParticipantKey;
import message.phase1.repository.ConversationParticipantRepository;

/** Class that interacts and acts as an adapter for <code>ConversationParticipantRepository</code> with <code>Facade</code> */
@Service
public class ConversationparticipantAdapter {
    
    @Autowired private ConversationParticipantRepository conversationParticipantRepository;
    @Autowired private EntityManager entityManager;

    public void addParticipant(Long conversationId, Long userId) {
        ConversationParticipantKey key = new ConversationParticipantKey();
        key.setConversation(conversationId);
        key.setUser(userId);
        ConversationParticipant participant = new ConversationParticipant();
        participant.setId(key);
        participant.setConversation(entityManager.getReference(Conversation.class, conversationId));
        participant.setUser(entityManager.getReference(Users.class, userId));
        conversationParticipantRepository.save(participant);
    }

    public List<Long> getParticipantsOfConversation(Long conversationId) {
        return conversationParticipantRepository.findUserIdsByConversationId(conversationId);
    }

    public List<Long> getUserConversations(Long userId) {
        return conversationParticipantRepository.findConversationIdsByUserId(userId);
    }

    public boolean isUserInConversation(Long conversationId, Long userId) {
        return getParticipantsOfConversation(conversationId).contains(userId);
    }
}
