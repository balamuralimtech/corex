package com.module.coretix.coretix;

import com.module.coretix.commonto.UserActivityTO;
import com.persist.coretix.modal.constants.GeneralConstants;
import com.persist.coretix.modal.coretix.dto.ChatContactSummary;
import com.persist.coretix.modal.coretix.dto.ChatConversationSummary;
import com.persist.coretix.modal.coretix.dto.ChatMessageView;

import java.util.Date;
import java.util.List;

public interface IUserChatService {

    List<ChatContactSummary> getAvailableContacts(int currentUserId, String currentUserType, Integer organizationId);

    List<ChatConversationSummary> getConversationSummaries(int currentUserId);

    List<ChatMessageView> getConversationMessages(int currentUserId, int conversationId);

    Date getLatestMessageTimestamp(int currentUserId, int conversationId);

    List<ChatMessageView> getConversationMessagesBetween(int currentUserId, int conversationId, Date fromInclusive, Date toExclusive);

    List<ChatMessageView> getConversationMessagesAfter(int currentUserId, int conversationId, Date afterExclusive);

    boolean hasConversationMessagesBefore(int currentUserId, int conversationId, Date beforeExclusive);

    Integer getConversationRecipientUserId(int currentUserId, int conversationId);

    Integer openConversation(int currentUserId, String currentUserType, Integer organizationId, int targetUserId);

    GeneralConstants sendMessage(UserActivityTO userActivityTO, int currentUserId, int conversationId, String message);

    GeneralConstants markConversationAsRead(int currentUserId, int conversationId);
}
