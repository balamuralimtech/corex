package com.persist.coretix.modal.coretix.dto;

import java.io.Serializable;
import java.util.Date;

public class ChatConversationSummary implements Serializable {

    private final int conversationId;
    private final int otherUserId;
    private final String otherUserName;
    private final String otherUserType;
    private final String organizationName;
    private final String lastMessage;
    private final Date lastMessageAt;
    private final Long unreadCount;

    public ChatConversationSummary(int conversationId, int otherUserId, String otherUserName, String otherUserType,
                                   String organizationName, String lastMessage, Date lastMessageAt, Long unreadCount) {
        this.conversationId = conversationId;
        this.otherUserId = otherUserId;
        this.otherUserName = otherUserName;
        this.otherUserType = otherUserType;
        this.organizationName = organizationName;
        this.lastMessage = lastMessage;
        this.lastMessageAt = lastMessageAt;
        this.unreadCount = unreadCount;
    }

    public int getConversationId() {
        return conversationId;
    }

    public int getOtherUserId() {
        return otherUserId;
    }

    public String getOtherUserName() {
        return otherUserName;
    }

    public String getOtherUserType() {
        return otherUserType;
    }

    public String getOrganizationName() {
        return organizationName;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public Date getLastMessageAt() {
        return lastMessageAt;
    }

    public long getUnreadCount() {
        return unreadCount == null ? 0L : unreadCount;
    }
}
