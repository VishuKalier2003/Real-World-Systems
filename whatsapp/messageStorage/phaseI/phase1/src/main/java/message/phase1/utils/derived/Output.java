package message.phase1.utils.derived;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/** Base class for <code>Endpoint</code> output. Stores a message and success flag */
@Getter
@Setter
@SuperBuilder
public class Output {
    private Boolean success;
    private String message;
}
