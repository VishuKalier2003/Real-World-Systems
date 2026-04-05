package message.phase1.utils.facade;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import message.phase1.api.dto.ConversationDto;
import message.phase1.exceptions.NoUserException;
import message.phase1.service.repo.ConversationAdapter;
import message.phase1.service.repo.UsersAdapter;

/** An Inner service class acting as facade for two loosely coupled adapters <code>UserAdapter</code> and <code>ConversationAdapter</code> */
@Service
public class Searching {
    
    @Autowired private UsersAdapter usersAdapter;
    @Autowired private ConversationAdapter conversationAdapter;

    public long createConversation(ConversationDto dto) {
        if(dto.getParticipants().stream().allMatch(userId -> usersAdapter.getUserById(userId) != null)) {
            return conversationAdapter.createConversation(dto.getType()).getConversationId();
        }
        throw new NoUserException("One or more participants does not exist");
    }
}
