package com.web.coretix.usermanagement;

import com.module.coretix.commonto.UserActivityTO;
import com.module.coretix.coretix.IUserChatService;
import com.module.coretix.usermanagement.IUserAdministrationService;
import com.persist.coretix.modal.constants.GeneralConstants;
import com.persist.coretix.modal.coretix.dto.ChatContactSummary;
import com.persist.coretix.modal.coretix.dto.ChatConversationSummary;
import com.persist.coretix.modal.coretix.dto.ChatMessageView;
import com.persist.coretix.modal.usermanagement.UserDetails;
import com.web.coretix.constants.SessionAttributes;
import com.web.coretix.constants.UserActivityConstants;
import com.web.coretix.general.NotificationService;
import com.web.coretix.general.SessionUtils;
import org.apache.commons.lang3.StringUtils;
import org.primefaces.PrimeFaces;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpSession;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Named("userChatBean")
@Scope("session")
public class UserChatBean implements Serializable {

    private static final long serialVersionUID = 8737292432605069100L;
    private static final Logger logger = LoggerFactory.getLogger(UserChatBean.class);
    private static final long ONE_WEEK_MILLIS = 7L * 24L * 60L * 60L * 1000L;

    private List<ChatContactSummary> availableContacts = new ArrayList<>();
    private List<ChatConversationSummary> conversationSummaries = new ArrayList<>();
    private List<ChatMessageView> conversationMessages = new ArrayList<>();
    private Integer selectedContactUserId;
    private Integer selectedConversationId;
    private String draftMessage;
    private String contactSearchTerm;
    private Date oldestLoadedMessageAt;
    private Date latestLoadedMessageAt;
    private boolean hasOlderMessages;
    private Map<Integer, String> profileImageSrcByUserId = new LinkedHashMap<>();

    @Inject
    private transient IUserChatService userChatService;

    @Inject
    private transient IUserAdministrationService userAdministrationService;

    public void initializePageAttributes() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        logger.info("initializePageAttributes invoked. postback={}, currentUserId={}, selectedContactUserId={}, selectedConversationId={}",
                facesContext != null && facesContext.isPostback(), getCurrentUserId(), selectedContactUserId, selectedConversationId);
        if (facesContext != null && !facesContext.isPostback()) {
            refreshChatState();
        }
    }

    public void refreshChatState() {
        logger.info("refreshChatState started. currentUserId={}, currentUserType={}, currentOrganizationId={}, selectedContactUserId={}, selectedConversationId={}",
                getCurrentUserId(), getCurrentUserType(), getCurrentOrganizationId(), selectedContactUserId, selectedConversationId);
        refreshConversationCollections();
        loadSelectedConversationMessages();
        logger.info("refreshChatState completed. selectedContactUserId={}, selectedConversationId={}, conversationMessagesCount={}",
                selectedContactUserId, selectedConversationId, conversationMessages.size());
    }

    public void autoRefreshChatState() {
        logger.debug("autoRefreshChatState invoked. selectedContactUserId={}, selectedConversationId={}",
                selectedContactUserId, selectedConversationId);
        String previousConversationListSignature = buildConversationListSignature();
        String previousThreadSignature = buildThreadSignature();
        Integer previousConversationId = selectedConversationId;
        refreshConversationCollections();
        if (!Objects.equals(previousConversationId, selectedConversationId)) {
            loadSelectedConversationMessages();
        } else {
            appendNewConversationMessages();
        }
        String currentConversationListSignature = buildConversationListSignature();
        String currentThreadSignature = buildThreadSignature();

        if (!Objects.equals(previousConversationListSignature, currentConversationListSignature)) {
            PrimeFaces.current().ajax().update("form:conversationListPanel");
        }
        if (!Objects.equals(previousThreadSignature, currentThreadSignature)) {
            PrimeFaces.current().ajax().update("form:threadHeaderPanel", "form:threadBodyPanel");
        }
    }

    public void loadOlderMessages() {
        logger.debug("loadOlderMessages invoked. selectedConversationId={}, hasOlderMessages={}, oldestLoadedMessageAt={}",
                selectedConversationId, hasOlderMessages, oldestLoadedMessageAt);
        boolean loadedOlderMessages = false;
        int olderMessagesCount = 0;

        if (selectedConversationId != null && hasOlderMessages && oldestLoadedMessageAt != null) {
            Date previousWindowEnd = oldestLoadedMessageAt;
            Date previousWindowStart = subtractWeek(previousWindowEnd);
            List<ChatMessageView> olderMessages = userChatService.getConversationMessagesBetween(
                    getCurrentUserId(), selectedConversationId, previousWindowStart, previousWindowEnd);
            olderMessagesCount = olderMessages.size();
            if (!olderMessages.isEmpty()) {
                conversationMessages = mergeMessages(olderMessages, conversationMessages);
                oldestLoadedMessageAt = conversationMessages.get(0).getCreatedAt();
                latestLoadedMessageAt = conversationMessages.get(conversationMessages.size() - 1).getCreatedAt();
                loadedOlderMessages = true;
            }
            hasOlderMessages = oldestLoadedMessageAt != null
                    && userChatService.hasConversationMessagesBefore(getCurrentUserId(), selectedConversationId, oldestLoadedMessageAt);
        }

        PrimeFaces.current().ajax().addCallbackParam("loadedOlderMessages", loadedOlderMessages);
        PrimeFaces.current().ajax().addCallbackParam("olderMessagesCount", olderMessagesCount);
        PrimeFaces.current().ajax().addCallbackParam("hasOlderMessages", hasOlderMessages);
    }

    public void openConversationFromContact() {
        logger.info("openConversationFromContact invoked. selectedContactUserId={}", selectedContactUserId);
        if (selectedContactUserId == null) {
            logger.warn("openConversationFromContact aborted because no contact is selected.");
            addMessage(FacesMessage.SEVERITY_WARN, "Warning", "Select a user to start chatting.");
            return;
        }
        openConversationWithUser(selectedContactUserId);
    }

    public void openConversationWithUser(Integer targetUserId) {
        logger.info("openConversationWithUser invoked. currentUserId={}, targetUserId={}, currentUserType={}, currentOrganizationId={}",
                getCurrentUserId(), targetUserId, getCurrentUserType(), getCurrentOrganizationId());
        if (targetUserId == null) {
            logger.warn("openConversationWithUser aborted because targetUserId is null.");
            addMessage(FacesMessage.SEVERITY_WARN, "Warning", "Select a user to start chatting.");
            return;
        }

        selectedContactUserId = targetUserId;
        selectedConversationId = null;
        conversationMessages = new ArrayList<>();

        boolean allowedContact = availableContacts.stream()
                .anyMatch(contact -> contact.getUserId() == targetUserId);
        logger.debug("openConversationWithUser contact validation completed. targetUserId={}, allowedContact={}, availableContactsCount={}",
                targetUserId, allowedContact, availableContacts.size());
        if (!allowedContact) {
            logger.warn("openConversationWithUser denied. targetUserId={} is not present in availableContacts.", targetUserId);
            addMessage(FacesMessage.SEVERITY_ERROR, "Error", "That chat is not allowed.");
            return;
        }

        Integer conversationId = userChatService.openConversation(
                getCurrentUserId(), getCurrentUserType(), getCurrentOrganizationId(), targetUserId);
        logger.info("openConversationWithUser service result. targetUserId={}, conversationId={}", targetUserId, conversationId);
        if (conversationId == null) {
            logger.error("openConversationWithUser failed to open conversation. currentUserId={}, targetUserId={}",
                    getCurrentUserId(), targetUserId);
            addMessage(FacesMessage.SEVERITY_ERROR, "Error", "Unable to open the chat conversation.");
            return;
        }

        selectedConversationId = conversationId;
        refreshChatState();
    }

    public void selectConversation(Integer conversationId) {
        logger.info("selectConversation invoked. currentUserId={}, requestedConversationId={}, previousConversationId={}",
                getCurrentUserId(), conversationId, selectedConversationId);
        selectedConversationId = conversationId;
        userChatService.markConversationAsRead(getCurrentUserId(), conversationId);
        refreshChatState();
    }

    public void sendMessage() {
        logger.info("sendMessage invoked. currentUserId={}, selectedContactUserId={}, selectedConversationId={}, draftBlank={}, draftLength={}",
                getCurrentUserId(), selectedContactUserId, selectedConversationId,
                StringUtils.isBlank(draftMessage), draftMessage == null ? 0 : draftMessage.length());
        if (selectedConversationId == null && selectedContactUserId != null) {
            logger.debug("sendMessage attempting to open a conversation because selectedConversationId is null. selectedContactUserId={}",
                    selectedContactUserId);
            Integer conversationId = userChatService.openConversation(
                    getCurrentUserId(), getCurrentUserType(), getCurrentOrganizationId(), selectedContactUserId);
            logger.info("sendMessage openConversation result. selectedContactUserId={}, conversationId={}",
                    selectedContactUserId, conversationId);
            if (conversationId != null) {
                selectedConversationId = conversationId;
            } else {
                logger.error("sendMessage aborted because conversation could not be opened. currentUserId={}, selectedContactUserId={}",
                        getCurrentUserId(), selectedContactUserId);
                addMessage(FacesMessage.SEVERITY_ERROR, "Error", "Unable to open the chat conversation.");
                return;
            }
        }

        if (selectedConversationId == null) {
            logger.warn("sendMessage aborted because no conversation is selected.");
            addMessage(FacesMessage.SEVERITY_WARN, "Warning", "Choose a conversation first.");
            return;
        }

        if (StringUtils.isBlank(draftMessage)) {
            logger.warn("sendMessage aborted because draftMessage is blank. selectedConversationId={}", selectedConversationId);
            addMessage(FacesMessage.SEVERITY_WARN, "Warning", "Enter a message.");
            return;
        }

        UserActivityTO userActivityTO = populateUserActivityTO();
        userActivityTO.setActivityType(UserActivityConstants.ADD.getValue());
        userActivityTO.setCreatedAt(new Date());

        GeneralConstants result = userChatService.sendMessage(userActivityTO, getCurrentUserId(), selectedConversationId, draftMessage);
        logger.info("sendMessage service completed. selectedConversationId={}, result={}", selectedConversationId, result);
        if (result == GeneralConstants.SUCCESSFUL) {
            Integer recipientUserId = userChatService.getConversationRecipientUserId(getCurrentUserId(), selectedConversationId);
            logger.debug("sendMessage resolved recipient. selectedConversationId={}, recipientUserId={}", selectedConversationId, recipientUserId);
            if (recipientUserId != null) {
                NotificationService.sendGrowlMessageToUserAccount(recipientUserId,
                        "New chat message from " + userActivityTO.getUserName());
            }
            draftMessage = "";
            refreshChatState();
            if (selectedConversationId != null) {
                logger.info("sendMessage completed successfully. selectedConversationId={}, conversationMessagesCount={}",
                        selectedConversationId, conversationMessages.size());
            } else {
                logger.warn("sendMessage persisted the message, but refresh left no selected conversation. selectedContactUserId={}, conversationMessagesCount={}",
                        selectedContactUserId, conversationMessages.size());
            }
            PrimeFaces.current().executeScript("scrollChatThreadToBottom();");
        } else {
            logger.error("sendMessage failed. currentUserId={}, selectedConversationId={}, result={}",
                    getCurrentUserId(), selectedConversationId, result);
            addMessage(FacesMessage.SEVERITY_ERROR, "Error",
                    "Unable to send the message. Please try again.");
        }
    }

    public String getSelectedConversationTitle() {
        ChatConversationSummary summary = getSelectedConversationSummary();
        if (summary != null) {
            return summary.getOtherUserName();
        }
        ChatContactSummary contact = getSelectedContactSummary();
        return contact == null ? "Chat" : contact.getUserName();
    }

    public String getSelectedConversationSubtitle() {
        ChatConversationSummary summary = getSelectedConversationSummary();
        if (summary != null) {
            String org = summary.getOrganizationName();
            if (StringUtils.isBlank(org)) {
                return summary.getOtherUserType();
            }
            return summary.getOtherUserType() + " • " + org;
        }
        ChatContactSummary contact = getSelectedContactSummary();
        if (contact == null) {
            return "Choose a user to start chatting.";
        }
        String org = contact.getOrganizationName();
        if (StringUtils.isBlank(org)) {
            return contact.getUserType();
        }
        return contact.getUserType() + " • " + org;
    }

    public boolean isOwnMessage(ChatMessageView messageView) {
        return messageView != null && messageView.getSenderUserId() == getCurrentUserId();
    }

    public boolean ownMessage(ChatMessageView messageView) {
        return isOwnMessage(messageView);
    }

    public boolean isActiveConversation(ChatConversationSummary summary) {
        return summary != null && Objects.equals(summary.getConversationId(), selectedConversationId);
    }

    public boolean activeConversation(ChatConversationSummary summary) {
        return isActiveConversation(summary);
    }

    public boolean isSelectedContact(ChatContactSummary contact) {
        return contact != null && Objects.equals(contact.getUserId(), selectedContactUserId);
    }

    public boolean selectedContact(ChatContactSummary contact) {
        return isSelectedContact(contact);
    }

    public boolean hasSelectedChatPeer() {
        return getSelectedContactSummary() != null || getSelectedConversationSummary() != null;
    }

    public String getEmptyThreadMessage() {
        ChatContactSummary contact = getSelectedContactSummary();
        if (contact != null) {
            return "Start the conversation with " + contact.getUserName() + ".";
        }
        return "Select a person from the left and start the conversation.";
    }

    public List<ChatContactSummary> getAvailableContacts() {
        return availableContacts;
    }

    public List<ChatContactSummary> getFilteredAvailableContacts() {
        if (StringUtils.isBlank(contactSearchTerm)) {
            return availableContacts;
        }

        String normalized = contactSearchTerm.trim().toLowerCase(Locale.ENGLISH);
        return availableContacts.stream()
                .filter(contact ->
                        (contact.getUserName() != null
                                && contact.getUserName().toLowerCase(Locale.ENGLISH).contains(normalized))
                        || (contact.getOrganizationName() != null
                                && contact.getOrganizationName().toLowerCase(Locale.ENGLISH).contains(normalized))
                        || (contact.getUserType() != null
                                && contact.getUserType().toLowerCase(Locale.ENGLISH).contains(normalized)))
                .collect(Collectors.toList());
    }

    public List<ChatConversationSummary> getConversationSummaries() {
        return conversationSummaries;
    }

    public List<ChatMessageView> getConversationMessages() {
        return conversationMessages;
    }

    public boolean isHasOlderMessages() {
        return hasOlderMessages;
    }

    public String getProfileImageSrc(Integer userId) {
        if (userId == null || userId <= 0) {
            return null;
        }
        return profileImageSrcByUserId.get(userId);
    }

    public String profileImageSrc(Integer userId) {
        return getProfileImageSrc(userId);
    }

    public Integer getSelectedChatPeerUserId() {
        ChatConversationSummary summary = getSelectedConversationSummary();
        if (summary != null) {
            return summary.getOtherUserId();
        }
        return selectedContactUserId;
    }

    public Integer getSelectedContactUserId() {
        return selectedContactUserId;
    }

    public void setSelectedContactUserId(Integer selectedContactUserId) {
        this.selectedContactUserId = selectedContactUserId;
    }

    public Integer getSelectedConversationId() {
        return selectedConversationId;
    }

    public void setSelectedConversationId(Integer selectedConversationId) {
        this.selectedConversationId = selectedConversationId;
    }

    public String getDraftMessage() {
        return draftMessage;
    }

    public void setDraftMessage(String draftMessage) {
        this.draftMessage = draftMessage;
    }

    public String getContactSearchTerm() {
        return contactSearchTerm;
    }

    public void setContactSearchTerm(String contactSearchTerm) {
        this.contactSearchTerm = contactSearchTerm;
    }

    private void loadSelectedConversationMessages() {
        if (selectedConversationId == null) {
            logger.debug("loadSelectedConversationMessages skipped because selectedConversationId is null.");
            resetConversationWindowState();
            return;
        }
        logger.debug("loadSelectedConversationMessages started. currentUserId={}, selectedConversationId={}",
                getCurrentUserId(), selectedConversationId);
        Date latestMessageTimestamp = userChatService.getLatestMessageTimestamp(getCurrentUserId(), selectedConversationId);
        if (latestMessageTimestamp == null) {
            resetConversationWindowState();
            userChatService.markConversationAsRead(getCurrentUserId(), selectedConversationId);
            return;
        }

        Date latestWindowStart = subtractWeek(latestMessageTimestamp);
        userChatService.markConversationAsRead(getCurrentUserId(), selectedConversationId);
        conversationMessages = new ArrayList<>(
                userChatService.getConversationMessagesBetween(getCurrentUserId(), selectedConversationId, latestWindowStart, null));
        oldestLoadedMessageAt = conversationMessages.isEmpty() ? null : conversationMessages.get(0).getCreatedAt();
        latestLoadedMessageAt = conversationMessages.isEmpty() ? null : conversationMessages.get(conversationMessages.size() - 1).getCreatedAt();
        hasOlderMessages = oldestLoadedMessageAt != null
                && userChatService.hasConversationMessagesBefore(getCurrentUserId(), selectedConversationId, oldestLoadedMessageAt);
        logger.debug("loadSelectedConversationMessages completed. selectedConversationId={}, conversationMessagesCount={}",
                selectedConversationId, conversationMessages.size());
    }

    private void refreshConversationCollections() {
        availableContacts = new ArrayList<>(userChatService.getAvailableContacts(getCurrentUserId(), getCurrentUserType(), getCurrentOrganizationId()));
        conversationSummaries = new ArrayList<>(userChatService.getConversationSummaries(getCurrentUserId()));
        refreshProfileImageCache();
        logger.debug("refreshConversationCollections fetched data. availableContactsCount={}, conversationSummariesCount={}",
                availableContacts.size(), conversationSummaries.size());

        if (selectedConversationId == null && !conversationSummaries.isEmpty()) {
            selectedConversationId = conversationSummaries.get(0).getConversationId();
            logger.debug("refreshConversationCollections auto-selected first conversation. selectedConversationId={}", selectedConversationId);
        } else if (selectedConversationId != null
                && conversationSummaries.stream().noneMatch(summary -> Objects.equals(summary.getConversationId(), selectedConversationId))) {
            selectedConversationId = conversationSummaries.isEmpty() ? null : conversationSummaries.get(0).getConversationId();
            logger.debug("refreshConversationCollections adjusted stale selected conversation. selectedConversationId={}", selectedConversationId);
        }
    }

    private void appendNewConversationMessages() {
        if (selectedConversationId == null) {
            resetConversationWindowState();
            return;
        }
        if (latestLoadedMessageAt == null) {
            loadSelectedConversationMessages();
            return;
        }

        List<ChatMessageView> newMessages = userChatService.getConversationMessagesAfter(
                getCurrentUserId(), selectedConversationId, latestLoadedMessageAt);
        if (!newMessages.isEmpty()) {
            conversationMessages = mergeMessages(conversationMessages, newMessages);
            oldestLoadedMessageAt = conversationMessages.get(0).getCreatedAt();
            latestLoadedMessageAt = conversationMessages.get(conversationMessages.size() - 1).getCreatedAt();
            userChatService.markConversationAsRead(getCurrentUserId(), selectedConversationId);
        }
        hasOlderMessages = oldestLoadedMessageAt != null
                && userChatService.hasConversationMessagesBefore(getCurrentUserId(), selectedConversationId, oldestLoadedMessageAt);
    }

    private ChatConversationSummary getSelectedConversationSummary() {
        if (selectedConversationId == null) {
            return null;
        }
        return conversationSummaries.stream()
                .filter(summary -> Objects.equals(summary.getConversationId(), selectedConversationId))
                .findFirst()
                .orElse(null);
    }

    private ChatContactSummary getSelectedContactSummary() {
        if (selectedContactUserId == null) {
            return null;
        }
        return availableContacts.stream()
                .filter(contact -> Objects.equals(contact.getUserId(), selectedContactUserId))
                .findFirst()
                .orElse(null);
    }

    private int getCurrentUserId() {
        Object userId = getSessionAttribute(SessionAttributes.USER_ACCOUNT_ID);
        return userId instanceof Integer ? (Integer) userId : 0;
    }

    private String getCurrentUserType() {
        Object userType = getSessionAttribute(SessionAttributes.USER_TYPE);
        return userType instanceof String ? (String) userType : null;
    }

    private Integer getCurrentOrganizationId() {
        Object organizationId = getSessionAttribute(SessionAttributes.ORGANIZATION_ID);
        return organizationId instanceof Integer ? (Integer) organizationId : null;
    }

    private Object getSessionAttribute(SessionAttributes attribute) {
        HttpSession session = SessionUtils.getSession();
        return session == null ? null : session.getAttribute(attribute.getName());
    }

    private UserActivityTO populateUserActivityTO() {
        HttpSession httpSession = SessionUtils.getSession();
        UserActivityTO userActivityTO = new UserActivityTO();

        if (httpSession != null) {
            userActivityTO.setUserId((Integer) httpSession.getAttribute(SessionAttributes.USER_ACCOUNT_ID.getName()));
            userActivityTO.setUserName((String) httpSession.getAttribute(SessionAttributes.USERNAME.getName()));
            userActivityTO.setIpAddress((String) httpSession.getAttribute(SessionAttributes.MACHINE_IP.getName()));
            userActivityTO.setDeviceInfo((String) httpSession.getAttribute(SessionAttributes.MACHINE_NAME.getName()));
            userActivityTO.setLocationInfo((String) httpSession.getAttribute(SessionAttributes.BROWSER_CLIENT_INFO.getName()));
        }

        return userActivityTO;
    }

    private List<ChatMessageView> mergeMessages(List<ChatMessageView> firstBatch, List<ChatMessageView> secondBatch) {
        Map<Integer, ChatMessageView> mergedById = new LinkedHashMap<>();
        for (ChatMessageView messageView : firstBatch) {
            mergedById.put(messageView.getMessageId(), messageView);
        }
        for (ChatMessageView messageView : secondBatch) {
            mergedById.put(messageView.getMessageId(), messageView);
        }
        return new ArrayList<>(mergedById.values());
    }

    private Date subtractWeek(Date anchor) {
        return anchor == null ? null : new Date(anchor.getTime() - ONE_WEEK_MILLIS);
    }

    private void resetConversationWindowState() {
        conversationMessages = new ArrayList<>();
        oldestLoadedMessageAt = null;
        latestLoadedMessageAt = null;
        hasOlderMessages = false;
    }

    private void refreshProfileImageCache() {
        Map<Integer, String> refreshedImages = new LinkedHashMap<>();
        for (UserDetails userDetails : userAdministrationService.getUserDetailsList()) {
            if (userDetails == null || userDetails.getUserId() <= 0) {
                continue;
            }
            String profileImageSrc = buildProfileImageSrc(userDetails);
            if (profileImageSrc != null) {
                refreshedImages.put(userDetails.getUserId(), profileImageSrc);
            }
        }
        profileImageSrcByUserId = refreshedImages;
    }

    private String buildProfileImageSrc(UserDetails userDetails) {
        if (userDetails == null
                || userDetails.getProfileImage() == null
                || userDetails.getProfileImage().length == 0
                || userDetails.getProfileImageContentType() == null) {
            return null;
        }
        return "data:" + userDetails.getProfileImageContentType() + ";base64,"
                + Base64.getEncoder().encodeToString(userDetails.getProfileImage());
    }

    private String buildConversationListSignature() {
        StringBuilder builder = new StringBuilder();
        for (ChatConversationSummary summary : conversationSummaries) {
            builder.append(summary.getConversationId()).append('|')
                    .append(summary.getUnreadCount()).append('|')
                    .append(summary.getLastMessageAt() == null ? "null" : summary.getLastMessageAt().getTime()).append('|')
                    .append(summary.getLastMessage() == null ? "" : summary.getLastMessage())
                    .append("||");
        }
        return builder.toString();
    }

    private String buildThreadSignature() {
        StringBuilder builder = new StringBuilder();
        builder.append(selectedConversationId == null ? "null" : selectedConversationId).append('|');
        for (ChatMessageView messageView : conversationMessages) {
            builder.append(messageView.getMessageId()).append('|')
                    .append(messageView.getSenderUserId()).append('|')
                    .append(messageView.getCreatedAt() == null ? "null" : messageView.getCreatedAt().getTime()).append('|')
                    .append(messageView.getMessage() == null ? "" : messageView.getMessage())
                    .append("||");
        }
        return builder.toString();
    }

    private void addMessage(javax.faces.application.FacesMessage.Severity severity, String summary, String detail) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(severity, summary, detail));
    }
}
