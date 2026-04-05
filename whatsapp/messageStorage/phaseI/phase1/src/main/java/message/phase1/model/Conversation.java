package message.phase1.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Entity for storing conversation data */
@Getter
@Setter
@Entity
@Table(name = "conversation")
@NoArgsConstructor
public class Conversation {
    
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long conversationId;

    @Column(nullable = true)
    private String type;        // single or group
    private LocalDateTime createdAt;
}
