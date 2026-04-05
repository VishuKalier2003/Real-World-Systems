package message.phase1.service.repo;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import message.phase1.model.Conversation;
import message.phase1.repository.ConversationRepository;

/** Class that interacts and acts as an adapter for <code>ConversationRepository</code> with <code>Facade</code> */
@Service
public class ConversationAdapter {

    @Autowired private ConversationRepository conversationRepository;
    
    public Conversation createConversation(String type) {
        Conversation conversation = new Conversation();
        if(type == null)
            type = "single";
        conversation.setType(type);
        conversation.setCreatedAt(LocalDateTime.now());
        conversationRepository.save(conversation);
        return conversation;
    }

    public Long getConversationId(Conversation conversation) {
        return conversation.getConversationId();
    }

    public Conversation getConversationById(Long conversationId) {
        return conversationRepository.findById(conversationId).orElse(null);
    }
}
