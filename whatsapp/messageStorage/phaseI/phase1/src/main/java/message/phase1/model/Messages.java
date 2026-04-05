package message.phase1.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Entity mapping storing message data, fetching <code>Conversation</code> and <code>Users</code> entity lazily */
@Getter
@Setter
@Entity
@Table(name = "messages", indexes = {
        @Index(name = "idx_conversation_timestamp", columnList = "conversation_id, timestamp")
    })
@NoArgsConstructor
public class Messages {

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long messageId;

    @ManyToOne(fetch = FetchType.LAZY)      // allows lazy reads (no need to use findByid to fetch entire DB)
    @JoinColumn(name = "conversation_id", nullable = false)
    private Conversation conversation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    private Users sender;

    private String content;
    private String type;        // content type: text, image, video, etc.
    @Column(nullable = false)
    private LocalDateTime timestamp;
}
