package com.persist.coretix.modal.coretix.dto;

import java.io.Serializable;
import java.util.Date;

public class ChatMessageView implements Serializable {

    private final int messageId;
    private final int conversationId;
    private final int senderUserId;
    private final String senderUserName;
    private final String message;
    private final Date createdAt;

    public ChatMessageView(int messageId, int conversationId, int senderUserId, String senderUserName,
                           String message, Date createdAt) {
        this.messageId = messageId;
        this.conversationId = conversationId;
        this.senderUserId = senderUserId;
        this.senderUserName = senderUserName;
        this.message = message;
        this.createdAt = createdAt;
    }

    public int getMessageId() {
        return messageId;
    }

    public int getConversationId() {
        return conversationId;
    }

    public int getSenderUserId() {
        return senderUserId;
    }

    public String getSenderUserName() {
        return senderUserName;
    }

    public String getMessage() {
        return message;
    }

    public Date getCreatedAt() {
        return createdAt;
    }
}
