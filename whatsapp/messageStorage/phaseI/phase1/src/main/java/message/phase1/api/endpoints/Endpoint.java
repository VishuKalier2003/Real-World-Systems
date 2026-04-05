package message.phase1.api.endpoints;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import message.phase1.api.dto.ConversationDto;
import message.phase1.api.dto.MessageDto;
import message.phase1.api.dto.UserDto;
import message.phase1.service.facade.Facade;
import message.phase1.utils.derived.Output;

/** Class stores API endpoints and each endpoint returning data as <code>Output</code> base class */
@RestController
@RequestMapping
public class Endpoint {

    @Autowired private Facade facade;       // Autowires only facade class following facade pattern

    @PostMapping("/add/user")
    public ResponseEntity<Output> addUser(@RequestBody UserDto userDto) {
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(facade.addUser(userDto));
    }

    @GetMapping("/user/{phone}")
    public ResponseEntity<Output> getUser(@PathVariable String phone) {
        return ResponseEntity.status(HttpStatus.OK).body(facade.getUserByPhone(phone));
    }

    @PostMapping("/start/conversation")
    public ResponseEntity<Output> startConversation(@RequestBody ConversationDto dto) {
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(facade.createConversation(dto));
    }

    @PostMapping("/send/message")
    public ResponseEntity<Output> sendMessage(@RequestBody MessageDto dto) {
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(facade.sendMessage(dto));
    }

    @GetMapping("/fetch/{conversationId}")
    public ResponseEntity<Output> fetchConversation(@PathVariable Long conversationId, @RequestParam(defaultValue="0") int page, @RequestParam(defaultValue="10") int size) {
        return ResponseEntity.status(HttpStatus.OK).body(facade.fetchConversation(conversationId, page, size));
    }

    @GetMapping("/fetch/all/{conversationId}")
    public ResponseEntity<Output> fetchFullConversation(@PathVariable Long conversationId) {
        return ResponseEntity.status(HttpStatus.OK).body(facade.fetchFullConversation(conversationId));
    }

    @GetMapping("/fetch/conversations/{userId}")
    public ResponseEntity<Output> getUserConversations(@PathVariable Long userId) {
        return ResponseEntity.status(HttpStatus.OK).body(facade.getUserConversations(userId));
    }

    @GetMapping("/fetch/participants/{conversationId}")
    public ResponseEntity<Output> getConversationParticipants(@PathVariable Long conversationId) {
        return ResponseEntity.status(HttpStatus.OK).body(facade.getConversationParticipants(conversationId));
    }
}
