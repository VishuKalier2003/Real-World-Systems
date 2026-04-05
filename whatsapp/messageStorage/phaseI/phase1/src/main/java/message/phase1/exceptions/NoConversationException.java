package message.phase1.exceptions;

/** Runtime exception class used when the Conversation is valid */
public class NoConversationException extends RuntimeException {
    public NoConversationException(String message) {
        super(message);
    }
}
