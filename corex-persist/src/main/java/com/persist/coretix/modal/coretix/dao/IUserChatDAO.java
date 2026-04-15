package com.persist.coretix.modal.coretix.dao;

import com.persist.coretix.modal.constants.GeneralConstants;
import com.persist.coretix.modal.coretix.dto.ChatContactSummary;
import com.persist.coretix.modal.coretix.dto.ChatConversationSummary;
import com.persist.coretix.modal.coretix.dto.ChatMessageView;

import java.util.Date;
import java.util.List;

public interface IUserChatDAO {

    List<ChatContactSummary> getAvailableContacts(int currentUserId, boolean applicationAdmin, Integer organizationId);

    ChatContactSummary getContactProfile(int userId);

    List<ChatConversationSummary> getConversationSummaries(int currentUserId);

    List<ChatMessageView> getConversationMessages(int conversationId);

    Date getLatestMessageTimestamp(int conversationId);

    List<ChatMessageView> getConversationMessagesBetween(int conversationId, Date fromInclusive, Date toExclusive);

    List<ChatMessageView> getConversationMessagesAfter(int conversationId, Date afterExclusive);

    boolean hasConversationMessagesBefore(int conversationId, Date beforeExclusive);

    boolean isConversationParticipant(int conversationId, int userId);

    Integer findConversationIdBetweenUsers(int firstUserId, int secondUserId);

    Integer getOtherParticipantUserId(int conversationId, int currentUserId);

    Integer createConversation(int createdByUserId, int firstUserId, int secondUserId);

    GeneralConstants addMessage(int conversationId, int senderUserId, String message);

    GeneralConstants markConversationAsRead(int conversationId, int userId);
}
