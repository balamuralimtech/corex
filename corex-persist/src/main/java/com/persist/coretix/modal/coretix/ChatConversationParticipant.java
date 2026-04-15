package com.persist.coretix.modal.coretix;

import com.persist.coretix.modal.usermanagement.UserDetails;

import javax.persistence.*;
import java.sql.Timestamp;

@Entity
@Table(name = "chat_conversation_participant",
        uniqueConstraints = @UniqueConstraint(name = "uk_chat_conversation_participant", columnNames = {"conversation_id", "user_id"}))
public class ChatConversationParticipant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conversation_id", nullable = false)
    private ChatConversation conversation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserDetails user;

    @Column(name = "created_at", nullable = false, updatable = false, insertable = false,
            columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private Timestamp createdAt;

    @Column(name = "last_read_at")
    private Timestamp lastReadAt;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public ChatConversation getConversation() {
        return conversation;
    }

    public void setConversation(ChatConversation conversation) {
        this.conversation = conversation;
    }

    public UserDetails getUser() {
        return user;
    }

    public void setUser(UserDetails user) {
        this.user = user;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public Timestamp getLastReadAt() {
        return lastReadAt;
    }

    public void setLastReadAt(Timestamp lastReadAt) {
        this.lastReadAt = lastReadAt;
    }
}
