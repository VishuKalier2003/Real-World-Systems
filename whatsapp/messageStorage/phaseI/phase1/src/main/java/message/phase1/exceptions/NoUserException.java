package message.phase1.exceptions;

/** Runtime exception class used when the user is invalid */
public class NoUserException extends RuntimeException {
    public NoUserException(String message) {
        super(message);
    }
    
}
