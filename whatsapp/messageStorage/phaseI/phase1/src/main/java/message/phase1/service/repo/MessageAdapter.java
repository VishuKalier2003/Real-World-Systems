package message.phase1.service.repo;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import message.phase1.api.dto.MessageDto;
import message.phase1.exceptions.NoConversationException;
import message.phase1.exceptions.NoUserException;
import message.phase1.model.Conversation;
import message.phase1.model.Messages;
import message.phase1.model.Users;
import message.phase1.repository.MessagesRepository;

/** Class that interacts and acts as an adapter for <code>MessageRepository</code> with <code>Facade</code> */
@Service
public class MessageAdapter {
    
    @Autowired private MessagesRepository messagesRepository;
    @Autowired private UsersAdapter usersAdapter;
    @Autowired private ConversationAdapter conversationAdapter;
    @Autowired private ConversationparticipantAdapter conversationParticipantAdapter;
    
    @PersistenceContext EntityManager entityManager;

    private static final int PAGE_SIZE = 50;

    public void sendMessage(MessageDto messageDto) {
        if(usersAdapter.getUserById(messageDto.getSenderId()) == null)
            throw new NoUserException("Sender does not exist for Id : "+messageDto.getSenderId());
        if(conversationAdapter.getConversationById(messageDto.getConversationId()) == null)
            throw new NoConversationException("Conversation does not exist for Id : "+messageDto.getConversationId());
        if(!conversationParticipantAdapter.isUserInConversation(messageDto.getConversationId(), messageDto.getSenderId()))
            throw new NoConversationException("Sender is not a participant of the conversation for Id : "+messageDto.getConversationId());
        // EntityManager's getReference works for PK
        Users sender = entityManager.getReference(Users.class, messageDto.getSenderId());
        Conversation conversation = entityManager.getReference(Conversation.class, messageDto.getConversationId());
        Messages message = new Messages();
        message.setSender(sender);
        message.setConversation(conversation);
        message.setContent(messageDto.getMessage());
        message.setType(messageDto.getType());
        message.setTimestamp(LocalDateTime.now());
        messagesRepository.save(message);
    }

    public List<Messages> fetch(Long conversationId, int page, int size) {
        if(conversationAdapter.getConversationById(conversationId) == null)
            throw new NoConversationException("Conversation does not exist for Id : "+conversationId);
        Pageable pageable = PageRequest.of(page, size, Sort.by("timestamp").descending());
        Page<Messages> messagesPage = messagesRepository.findByConversationConversationId(conversationId, pageable);
        return messagesPage.getContent();
    }

    public List<Messages> fetchAll(Long conversationId) {
        if(conversationAdapter.getConversationById(conversationId) == null)
            throw new NoConversationException("Conversation does not exist for Id : "+conversationId);
        int page = 0;
        List<Messages> allMessages = new ArrayList<>();
        while(true) {
            List<Messages> messagesPage = fetch(conversationId, page, PAGE_SIZE);
            if(messagesPage.isEmpty()) {
                break;
            }
            allMessages.addAll(messagesPage);
            page++;
        }
        return allMessages;
    }
}
