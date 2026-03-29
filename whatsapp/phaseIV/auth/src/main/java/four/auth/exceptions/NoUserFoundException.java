package four.auth.exceptions;

public class NoUserFoundException extends RuntimeException {
    public NoUserFoundException() {
        super("No user found");
    }
}