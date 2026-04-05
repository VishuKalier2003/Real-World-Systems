package message.phase1.global;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import message.phase1.exceptions.NoConversationException;
import message.phase1.exceptions.NoUserException;
import message.phase1.utils.derived.Output;

/** Global exception handler for endpoints handling <code>NoConversationException</code>, <code>NoUserException</code> and <code>RuntimeException</code> */
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(NoConversationException.class)
    public ResponseEntity<Output> handleConversationNotFound(NoConversationException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Output.builder()
                        .success(false)
                        .message(ex.getMessage())
                        .build());
    }

    @ExceptionHandler(NoUserException.class)
    public ResponseEntity<Output> handleUserNotFound(NoUserException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Output.builder()
                        .success(false)
                        .message(ex.getMessage())
                        .build());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Output> handleGenericException(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Output.builder()
                        .success(false)
                        .message("Internal server error: " + ex.getMessage())
                        .build());
    }
}