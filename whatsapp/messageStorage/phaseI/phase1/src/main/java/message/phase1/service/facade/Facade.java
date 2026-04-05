package message.phase1.service.facade;

import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import message.phase1.api.dto.ConversationDto;
import message.phase1.api.dto.MessageDto;
import message.phase1.api.dto.UserDto;
import message.phase1.service.repo.ConversationparticipantAdapter;
import message.phase1.service.repo.MessageAdapter;
import message.phase1.service.repo.UsersAdapter;
import message.phase1.utils.derived.Output;
import message.phase1.utils.derived.OutputConversationID;
import message.phase1.utils.derived.OutputConversationsID;
import message.phase1.utils.derived.OutputListOfConversationID;
import message.phase1.utils.derived.OutputListOfUserID;
import message.phase1.utils.derived.OutputUserID;
import message.phase1.utils.facade.Searching;

/** Class to define functions that directly interact with the <code>Endpoint</code> returning data as <code>Output</code> base class */
@Service
public class Facade {
    
    @Autowired private UsersAdapter usersAdapter;
    @Autowired private Searching searching;
    @Autowired private MessageAdapter messageAdapter;
    @Autowired private ConversationparticipantAdapter conversationParticipantAdapter;

    public OutputUserID addUser(UserDto dto) {
        Long userId = usersAdapter.save(dto);
        return OutputUserID.builder()
            .success(true)
            .message("User added successfully")
            .userID(userId)
            .build();
    }

    public OutputUserID getUserByPhone(String phone) {
        Long userId = usersAdapter.getUserIdByPhone(phone);
        if(userId == Long.MIN_VALUE) {
            return OutputUserID.builder()
                .success(false)
                .message("User not found")
                .userID(null)
                .build();
        }
        return OutputUserID.builder()
            .success(true)
            .message("User found")
            .userID(userId)
            .build();
    }

    public OutputConversationID createConversation(ConversationDto dto) {
        try {
            long conversationId = searching.createConversation(dto);
            return OutputConversationID.builder()
                .success(true)
                .message("Conversation created successfully")
                .conversationId(conversationId)
                .build();
        } catch (Exception e) {
            return OutputConversationID.builder()
                .success(false)
                .message("Failed to create conversation for "+e.getMessage())
                .conversationId(null)
                .build();
        }
    }

    public OutputConversationsID fetchConversation(Long conversationId, int page, int size) {
        try {
            return OutputConversationsID.builder()
                .success(true)
                .message("Messages fetched successfully")
                .messageList(messageAdapter.fetch(conversationId, page, size))
                .build();
        } catch (Exception e) {
            return OutputConversationsID.builder()
                .success(false)
                .message("Failed to fetch messages for "+e.getMessage())
                .messageList(new ArrayList<>())
                .build();
        }
    }

    public OutputConversationsID fetchFullConversation(Long conversationId) {
        try {
            return OutputConversationsID.builder()
                .success(true)
                .message("Messages fetched successfully")
                .messageList(messageAdapter.fetchAll(conversationId))
                .build();
        } catch (Exception e) {
            return OutputConversationsID.builder()
                .success(false)
                .message("Failed to fetch messages for "+e.getMessage())
                .messageList(new ArrayList<>())
                .build();
        }
    }

    public Output sendMessage(MessageDto dto) {
        try {
            messageAdapter.sendMessage(dto);
            return Output.builder()
                .success(true)
                .message("Message sent successfully")
                .build();
        } catch (Exception e) {
            return Output.builder()
                .success(false)
                .message("Failed to send message for "+e.getMessage())
                .build();
        }
    }

    public OutputListOfUserID getConversationParticipants(Long conversationId) {
        try {
            return OutputListOfUserID.builder()
                .success(true)
                .message("Participants fetched successfully")
                .userIDs(conversationParticipantAdapter.getParticipantsOfConversation(conversationId))
                .build();
        } catch (Exception e) {
            return OutputListOfUserID.builder()
                .success(false)
                .message("Failed to fetch participants for "+e.getMessage())
                .userIDs(new ArrayList<>())
                .build();
        }
    }

    public OutputListOfConversationID getUserConversations(Long userId) {
        try {
            return OutputListOfConversationID.builder()
                .success(true)
                .message("Conversations fetched successfully")
                .conversationIDs(conversationParticipantAdapter.getUserConversations(userId))
                .build();
        } catch (Exception e) {
            return OutputListOfConversationID.builder()
                .success(false)
                .message("Failed to fetch conversations for "+e.getMessage())
                .conversationIDs(new ArrayList<>())
                .build();
        }
    }
}
