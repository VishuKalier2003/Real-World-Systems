package message.phase1.utils.derived;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/** Subclass of <code>Output</code> storing userId */
@Getter
@Setter
@SuperBuilder
public class OutputUserID extends Output {
    private Long userID;
}
