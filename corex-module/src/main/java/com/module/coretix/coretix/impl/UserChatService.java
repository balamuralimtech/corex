package com.module.coretix.coretix.impl;

import com.module.coretix.commonto.UserActivityTO;
import com.module.coretix.coretix.IUserChatService;
import com.persist.coretix.modal.constants.GeneralConstants;
import com.persist.coretix.modal.coretix.dao.IUserChatDAO;
import com.persist.coretix.modal.coretix.dto.ChatContactSummary;
import com.persist.coretix.modal.coretix.dto.ChatConversationSummary;
import com.persist.coretix.modal.coretix.dto.ChatMessageView;
import com.persist.coretix.modal.usermanagement.UserActivities;
import com.persist.coretix.modal.usermanagement.dao.impl.UserActivityDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import javax.inject.Named;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Collections;
import java.util.List;

@Named
@Transactional(readOnly = true)
public class UserChatService implements IUserChatService {

    private static final Logger logger = LoggerFactory.getLogger(UserChatService.class);
    private static final String APPLICATION_ADMIN = "APPLICATION_ADMIN";

    @Inject
    private IUserChatDAO userChatDAO;

    @Inject
    private UserActivityDAO userActivityDAO;

    @Override
    public List<ChatContactSummary> getAvailableContacts(int currentUserId, String currentUserType, Integer organizationId) {
        return userChatDAO.getAvailableContacts(currentUserId,
                isApplicationAdmin(currentUserType),
                organizationId);
    }

    @Override
    public List<ChatConversationSummary> getConversationSummaries(int currentUserId) {
        return userChatDAO.getConversationSummaries(currentUserId);
    }

    @Override
    public List<ChatMessageView> getConversationMessages(int currentUserId, int conversationId) {
        if (!userChatDAO.isConversationParticipant(conversationId, currentUserId)) {
            return Collections.emptyList();
        }
        return userChatDAO.getConversationMessages(conversationId);
    }

    @Override
    public Date getLatestMessageTimestamp(int currentUserId, int conversationId) {
        if (!userChatDAO.isConversationParticipant(conversationId, currentUserId)) {
            return null;
        }
        return userChatDAO.getLatestMessageTimestamp(conversationId);
    }

    @Override
    public List<ChatMessageView> getConversationMessagesBetween(int currentUserId, int conversationId, Date fromInclusive, Date toExclusive) {
        if (!userChatDAO.isConversationParticipant(conversationId, currentUserId)) {
            return Collections.emptyList();
        }
        return userChatDAO.getConversationMessagesBetween(conversationId, fromInclusive, toExclusive);
    }

    @Override
    public List<ChatMessageView> getConversationMessagesAfter(int currentUserId, int conversationId, Date afterExclusive) {
        if (!userChatDAO.isConversationParticipant(conversationId, currentUserId)) {
            return Collections.emptyList();
        }
        return userChatDAO.getConversationMessagesAfter(conversationId, afterExclusive);
    }

    @Override
    public boolean hasConversationMessagesBefore(int currentUserId, int conversationId, Date beforeExclusive) {
        if (!userChatDAO.isConversationParticipant(conversationId, currentUserId)) {
            return false;
        }
        return userChatDAO.hasConversationMessagesBefore(conversationId, beforeExclusive);
    }

    @Override
    public Integer getConversationRecipientUserId(int currentUserId, int conversationId) {
        if (conversationId <= 0 || currentUserId <= 0) {
            return null;
        }
        if (!userChatDAO.isConversationParticipant(conversationId, currentUserId)) {
            return null;
        }
        return userChatDAO.getOtherParticipantUserId(conversationId, currentUserId);
    }

    @Override
    @Transactional(readOnly = false)
    public Integer openConversation(int currentUserId, String currentUserType, Integer organizationId, int targetUserId) {
        if (currentUserId <= 0 || targetUserId <= 0 || currentUserId == targetUserId) {
            logger.warn("Invalid chat open request. currentUserId={}, targetUserId={}", currentUserId, targetUserId);
            return null;
        }

        ChatContactSummary currentUserProfile = userChatDAO.getContactProfile(currentUserId);
        ChatContactSummary targetProfile = userChatDAO.getContactProfile(targetUserId);
        if (!isAllowedPeer(currentUserType, organizationId, currentUserProfile, targetProfile)) {
            logger.warn("Chat open denied. currentUserId={}, targetUserId={}, currentType={}, sessionOrganizationId={}, currentProfileOrg={}, targetProfileOrg={}, targetType={}",
                    currentUserId, targetUserId, currentUserType, organizationId,
                    currentUserProfile == null ? null : currentUserProfile.getOrganizationId(),
                    targetProfile == null ? null : targetProfile.getOrganizationId(),
                    targetProfile == null ? null : targetProfile.getUserType());
            return null;
        }

        Integer existingConversationId = userChatDAO.findConversationIdBetweenUsers(currentUserId, targetUserId);
        if (existingConversationId != null) {
            return existingConversationId;
        }

        Integer createdConversationId = userChatDAO.createConversation(currentUserId, currentUserId, targetUserId);
        if (createdConversationId == null) {
            logger.error("Chat conversation creation failed. currentUserId={}, targetUserId={}", currentUserId, targetUserId);
        }
        return createdConversationId;
    }

    @Override
    @Transactional(readOnly = false)
    public GeneralConstants sendMessage(UserActivityTO userActivityTO, int currentUserId, int conversationId, String message) {
        logger.info("sendMessage called. currentUserId={}, conversationId={}, hasMessage={}",
                currentUserId, conversationId, message != null && !message.trim().isEmpty());
        if (conversationId <= 0 || currentUserId <= 0 || message == null || message.trim().isEmpty()) {
            return GeneralConstants.FAILED;
        }
        if (!userChatDAO.isConversationParticipant(conversationId, currentUserId)) {
            logger.warn("sendMessage denied. currentUserId={} is not participant in conversationId={}", currentUserId, conversationId);
            return GeneralConstants.FAILED;
        }

        GeneralConstants result = userChatDAO.addMessage(conversationId, currentUserId, message.trim());
        logger.info("sendMessage DAO result={}", result);
        if (result == GeneralConstants.SUCCESSFUL && userActivityTO != null) {
            userActivityTO.setActivityDescription("Chat message sent");
            addUserActivity(userActivityTO);
        }
        return result;
    }

    @Override
    @Transactional(readOnly = false)
    public GeneralConstants markConversationAsRead(int currentUserId, int conversationId) {
        if (conversationId <= 0 || currentUserId <= 0) {
            return GeneralConstants.FAILED;
        }
        if (!userChatDAO.isConversationParticipant(conversationId, currentUserId)) {
            return GeneralConstants.FAILED;
        }
        return userChatDAO.markConversationAsRead(conversationId, currentUserId);
    }

    private boolean isApplicationAdmin(String userType) {
        return userType != null && APPLICATION_ADMIN.equalsIgnoreCase(userType.trim());
    }

    private boolean isAllowedPeer(String currentUserType, Integer sessionOrganizationId,
                                  ChatContactSummary currentUserProfile, ChatContactSummary targetProfile) {
        if (targetProfile == null) {
            return false;
        }

        if (isApplicationAdmin(currentUserType)
                || (currentUserProfile != null && isApplicationAdmin(currentUserProfile.getUserType()))
                || isApplicationAdmin(targetProfile.getUserType())) {
            return true;
        }

        Integer effectiveOrganizationId = sessionOrganizationId;
        if (effectiveOrganizationId == null && currentUserProfile != null) {
            effectiveOrganizationId = currentUserProfile.getOrganizationId();
        }

        return effectiveOrganizationId != null && effectiveOrganizationId.equals(targetProfile.getOrganizationId());
    }

    private void addUserActivity(UserActivityTO userActivityTO) {
        UserActivities useractivity = new UserActivities();
        useractivity.setUserId(userActivityTO.getUserId());
        useractivity.setUserName(userActivityTO.getUserName());
        useractivity.setDeviceInfo(userActivityTO.getDeviceInfo());
        useractivity.setIpAddress(userActivityTO.getIpAddress());
        useractivity.setLocationInfo(userActivityTO.getLocationInfo());
        useractivity.setActivityType(userActivityTO.getActivityType());
        useractivity.setActivityDescription(userActivityTO.getActivityDescription());
        useractivity.setCreatedAt(new Timestamp(System.currentTimeMillis()));
        userActivityDAO.addUserActivity(useractivity);
    }
}
