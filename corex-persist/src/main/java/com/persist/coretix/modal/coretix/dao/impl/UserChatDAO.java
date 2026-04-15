package com.persist.coretix.modal.coretix.dao.impl;

import com.persist.coretix.modal.constants.GeneralConstants;
import com.persist.coretix.modal.coretix.ChatConversation;
import com.persist.coretix.modal.coretix.ChatConversationParticipant;
import com.persist.coretix.modal.coretix.ChatMessage;
import com.persist.coretix.modal.coretix.dao.IUserChatDAO;
import com.persist.coretix.modal.coretix.dto.ChatContactSummary;
import com.persist.coretix.modal.coretix.dto.ChatConversationSummary;
import com.persist.coretix.modal.coretix.dto.ChatMessageView;
import com.persist.coretix.modal.usermanagement.UserDetails;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Collections;
import java.util.List;

@Named
public class UserChatDAO implements IUserChatDAO {

    private static final Logger logger = LoggerFactory.getLogger(UserChatDAO.class);

    @Inject
    private SessionFactory sessionFactory;

    @Override
    @SuppressWarnings("unchecked")
    public List<ChatContactSummary> getAvailableContacts(int currentUserId, boolean applicationAdmin, Integer organizationId) {
        Session session = null;
        Transaction trans = null;
        try {
            session = sessionFactory.openSession();
            trans = session.beginTransaction();
            String hql = "select new com.persist.coretix.modal.coretix.dto.ChatContactSummary(" +
                    "u.userId, u.userName, u.userType, org.id, org.organizationName) " +
                    "from UserDetails u left join u.organization org " +
                    "where u.userId <> :currentUserId and u.accountDisabled = false ";
            if (!applicationAdmin) {
                hql += "and (upper(u.userType) = 'APPLICATION_ADMIN' or org.id = :organizationId) ";
            }
            hql += "order by case when upper(u.userType) = 'APPLICATION_ADMIN' then 0 else 1 end, lower(u.userName)";

            org.hibernate.query.Query<ChatContactSummary> query = session.createQuery(hql);
            query.setParameter("currentUserId", currentUserId);
            if (!applicationAdmin) {
                query.setParameter("organizationId", organizationId);
            }
            List<ChatContactSummary> contacts = query.list();
            trans.commit();
            return contacts;
        } catch (Exception e) {
            rollback(trans);
            logger.error("Unable to fetch chat contacts for user {}", currentUserId, e);
            return Collections.emptyList();
        } finally {
            close(session);
        }
    }

    @Override
    public ChatContactSummary getContactProfile(int userId) {
        Session session = null;
        Transaction trans = null;
        try {
            session = sessionFactory.openSession();
            trans = session.beginTransaction();
            ChatContactSummary profile = session.createQuery(
                            "select new com.persist.coretix.modal.coretix.dto.ChatContactSummary(" +
                                    "u.userId, u.userName, u.userType, org.id, org.organizationName) " +
                                    "from UserDetails u left join u.organization org " +
                                    "where u.userId = :userId and u.accountDisabled = false",
                            ChatContactSummary.class)
                    .setParameter("userId", userId)
                    .uniqueResult();
            trans.commit();
            return profile;
        } catch (Exception e) {
            rollback(trans);
            logger.error("Unable to fetch chat profile for user {}", userId, e);
            return null;
        } finally {
            close(session);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<ChatConversationSummary> getConversationSummaries(int currentUserId) {
        Session session = null;
        Transaction trans = null;
        try {
            session = sessionFactory.openSession();
            trans = session.beginTransaction();
            List<ChatConversationSummary> summaries = session.createQuery(
                            "select new com.persist.coretix.modal.coretix.dto.ChatConversationSummary(" +
                                    "c.id, otherParticipant.user.userId, otherParticipant.user.userName, " +
                                    "otherParticipant.user.userType, org.organizationName, " +
                                    "(select latest.message from ChatMessage latest where latest.id = (" +
                                    " select max(m2.id) from ChatMessage m2 where m2.conversation.id = c.id)), " +
                                    "c.lastMessageAt, " +
                                    "(select count(m3.id) from ChatMessage m3 where m3.conversation.id = c.id " +
                                    " and m3.sender.userId <> :currentUserId " +
                                    " and (selfParticipant.lastReadAt is null or m3.createdAt > selfParticipant.lastReadAt))) " +
                                    "from ChatConversationParticipant selfParticipant " +
                                    "join selfParticipant.conversation c " +
                                    "join c.participants otherParticipant " +
                                    "left join otherParticipant.user.organization org " +
                                    "where selfParticipant.user.userId = :currentUserId " +
                                    "and otherParticipant.user.userId <> :currentUserId " +
                                    "order by c.lastMessageAt desc nulls last, c.id desc",
                            ChatConversationSummary.class)
                    .setParameter("currentUserId", currentUserId)
                    .list();
            trans.commit();
            return summaries;
        } catch (Exception e) {
            rollback(trans);
            logger.error("Unable to fetch chat conversation summaries for user {}", currentUserId, e);
            return Collections.emptyList();
        } finally {
            close(session);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<ChatMessageView> getConversationMessages(int conversationId) {
        return getConversationMessagesBetween(conversationId, null, null);
    }

    @Override
    public Date getLatestMessageTimestamp(int conversationId) {
        Session session = null;
        Transaction trans = null;
        try {
            session = sessionFactory.openSession();
            trans = session.beginTransaction();
            Date latestMessageAt = session.createQuery(
                            "select max(m.createdAt) from ChatMessage m where m.conversation.id = :conversationId",
                            Date.class)
                    .setParameter("conversationId", conversationId)
                    .uniqueResult();
            trans.commit();
            return latestMessageAt;
        } catch (Exception e) {
            rollback(trans);
            logger.error("Unable to fetch latest chat message timestamp for conversation {}", conversationId, e);
            return null;
        } finally {
            close(session);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<ChatMessageView> getConversationMessagesBetween(int conversationId, Date fromInclusive, Date toExclusive) {
        Session session = null;
        Transaction trans = null;
        try {
            session = sessionFactory.openSession();
            trans = session.beginTransaction();
            StringBuilder hql = new StringBuilder(
                    "select new com.persist.coretix.modal.coretix.dto.ChatMessageView(" +
                            "m.id, m.conversation.id, m.sender.userId, m.sender.userName, m.message, m.createdAt) " +
                            "from ChatMessage m where m.conversation.id = :conversationId ");
            if (fromInclusive != null) {
                hql.append("and m.createdAt >= :fromInclusive ");
            }
            if (toExclusive != null) {
                hql.append("and m.createdAt < :toExclusive ");
            }
            hql.append("order by m.createdAt asc, m.id asc");

            org.hibernate.query.Query<ChatMessageView> query = session.createQuery(hql.toString(), ChatMessageView.class)
                    .setParameter("conversationId", conversationId);
            if (fromInclusive != null) {
                query.setParameter("fromInclusive", fromInclusive);
            }
            if (toExclusive != null) {
                query.setParameter("toExclusive", toExclusive);
            }
            List<ChatMessageView> messages = query.list();
            trans.commit();
            return messages;
        } catch (Exception e) {
            rollback(trans);
            logger.error("Unable to fetch chat messages for conversation {} between {} and {}", conversationId, fromInclusive, toExclusive, e);
            return Collections.emptyList();
        } finally {
            close(session);
        }
    }

    @Override
    public List<ChatMessageView> getConversationMessagesAfter(int conversationId, Date afterExclusive) {
        if (afterExclusive == null) {
            return getConversationMessages(conversationId);
        }
        return getConversationMessagesBetween(conversationId, new Date(afterExclusive.getTime() + 1L), null);
    }

    @Override
    public boolean hasConversationMessagesBefore(int conversationId, Date beforeExclusive) {
        if (beforeExclusive == null) {
            return false;
        }
        Session session = null;
        Transaction trans = null;
        try {
            session = sessionFactory.openSession();
            trans = session.beginTransaction();
            Long count = session.createQuery(
                            "select count(m.id) from ChatMessage m where m.conversation.id = :conversationId and m.createdAt < :beforeExclusive",
                            Long.class)
                    .setParameter("conversationId", conversationId)
                    .setParameter("beforeExclusive", beforeExclusive)
                    .uniqueResult();
            trans.commit();
            return count != null && count > 0;
        } catch (Exception e) {
            rollback(trans);
            logger.error("Unable to check older chat messages for conversation {} before {}", conversationId, beforeExclusive, e);
            return false;
        } finally {
            close(session);
        }
    }

    @Override
    public boolean isConversationParticipant(int conversationId, int userId) {
        Session session = null;
        Transaction trans = null;
        try {
            session = sessionFactory.openSession();
            trans = session.beginTransaction();
            Long count = session.createQuery(
                            "select count(p.id) from ChatConversationParticipant p " +
                                    "where p.conversation.id = :conversationId and p.user.userId = :userId",
                            Long.class)
                    .setParameter("conversationId", conversationId)
                    .setParameter("userId", userId)
                    .uniqueResult();
            trans.commit();
            return count != null && count > 0;
        } catch (Exception e) {
            rollback(trans);
            logger.error("Unable to verify chat participant {} in conversation {}", userId, conversationId, e);
            return false;
        } finally {
            close(session);
        }
    }

    @Override
    public Integer findConversationIdBetweenUsers(int firstUserId, int secondUserId) {
        Session session = null;
        Transaction trans = null;
        try {
            session = sessionFactory.openSession();
            trans = session.beginTransaction();
            Integer conversationId = session.createQuery(
                            "select p1.conversation.id from ChatConversationParticipant p1, ChatConversationParticipant p2 " +
                                    "where p1.conversation.id = p2.conversation.id " +
                                    "and p1.user.userId = :firstUserId and p2.user.userId = :secondUserId",
                            Integer.class)
                    .setParameter("firstUserId", firstUserId)
                    .setParameter("secondUserId", secondUserId)
                    .setMaxResults(1)
                    .uniqueResult();
            trans.commit();
            return conversationId;
        } catch (Exception e) {
            rollback(trans);
            logger.error("Unable to find chat conversation between {} and {}", firstUserId, secondUserId, e);
            return null;
        } finally {
            close(session);
        }
    }

    @Override
    public Integer getOtherParticipantUserId(int conversationId, int currentUserId) {
        Session session = null;
        Transaction trans = null;
        try {
            session = sessionFactory.openSession();
            trans = session.beginTransaction();
            Integer userId = session.createQuery(
                            "select p.user.userId from ChatConversationParticipant p " +
                                    "where p.conversation.id = :conversationId and p.user.userId <> :currentUserId",
                            Integer.class)
                    .setParameter("conversationId", conversationId)
                    .setParameter("currentUserId", currentUserId)
                    .setMaxResults(1)
                    .uniqueResult();
            trans.commit();
            return userId;
        } catch (Exception e) {
            rollback(trans);
            logger.error("Unable to resolve other participant for conversation {} and user {}", conversationId, currentUserId, e);
            return null;
        } finally {
            close(session);
        }
    }

    @Override
    public Integer createConversation(int createdByUserId, int firstUserId, int secondUserId) {
        Session session = null;
        Transaction trans = null;
        try {
            session = sessionFactory.openSession();
            trans = session.beginTransaction();

            UserDetails createdBy = session.get(UserDetails.class, createdByUserId);
            UserDetails firstUser = session.get(UserDetails.class, firstUserId);
            UserDetails secondUser = session.get(UserDetails.class, secondUserId);
            if (createdBy == null || firstUser == null || secondUser == null) {
                trans.rollback();
                return null;
            }

            ChatConversation conversation = new ChatConversation();
            conversation.setCreatedBy(createdBy);
            session.save(conversation);

            Timestamp now = new Timestamp(System.currentTimeMillis());
            ChatConversationParticipant firstParticipant = new ChatConversationParticipant();
            firstParticipant.setConversation(conversation);
            firstParticipant.setUser(firstUser);
            firstParticipant.setLastReadAt(now);
            session.save(firstParticipant);

            ChatConversationParticipant secondParticipant = new ChatConversationParticipant();
            secondParticipant.setConversation(conversation);
            secondParticipant.setUser(secondUser);
            session.save(secondParticipant);

            trans.commit();
            return conversation.getId();
        } catch (Exception e) {
            rollback(trans);
            logger.error("Unable to create chat conversation between {} and {}", firstUserId, secondUserId, e);
            return null;
        } finally {
            close(session);
        }
    }

    @Override
    public GeneralConstants addMessage(int conversationId, int senderUserId, String message) {
        Session session = null;
        Transaction trans = null;
        try {
            session = sessionFactory.openSession();
            trans = session.beginTransaction();

            ChatConversation conversation = session.get(ChatConversation.class, conversationId);
            UserDetails sender = session.get(UserDetails.class, senderUserId);
            if (conversation == null || sender == null) {
                logger.warn("addMessage missing conversation or sender. conversationId={}, senderUserId={}, conversationFound={}, senderFound={}",
                        conversationId, senderUserId, conversation != null, sender != null);
                trans.rollback();
                return GeneralConstants.ENTRY_NOT_EXISTS;
            }

            Long participantCount = session.createQuery(
                            "select count(p.id) from ChatConversationParticipant p " +
                                    "where p.conversation.id = :conversationId and p.user.userId = :senderUserId",
                            Long.class)
                    .setParameter("conversationId", conversationId)
                    .setParameter("senderUserId", senderUserId)
                    .uniqueResult();
            if (participantCount == null || participantCount == 0) {
                logger.warn("addMessage sender is not a participant. conversationId={}, senderUserId={}", conversationId, senderUserId);
                trans.rollback();
                return GeneralConstants.FAILED;
            }

            ChatMessage chatMessage = new ChatMessage();
            chatMessage.setConversation(conversation);
            chatMessage.setSender(sender);
            chatMessage.setMessage(message);
            session.save(chatMessage);

            Timestamp now = new Timestamp(System.currentTimeMillis());
            conversation.setLastMessageAt(now);
            session.update(conversation);

            session.createQuery("update ChatConversationParticipant p set p.lastReadAt = :now " +
                            "where p.conversation.id = :conversationId and p.user.userId = :senderUserId")
                    .setParameter("now", now)
                    .setParameter("conversationId", conversationId)
                    .setParameter("senderUserId", senderUserId)
                    .executeUpdate();

            trans.commit();
            logger.info("addMessage committed successfully. conversationId={}, senderUserId={}", conversationId, senderUserId);
            return GeneralConstants.SUCCESSFUL;
        } catch (Exception e) {
            rollback(trans);
            logger.error("Unable to add chat message for conversation {}", conversationId, e);
            return GeneralConstants.FAILED;
        } finally {
            close(session);
        }
    }

    @Override
    public GeneralConstants markConversationAsRead(int conversationId, int userId) {
        Session session = null;
        Transaction trans = null;
        try {
            session = sessionFactory.openSession();
            trans = session.beginTransaction();
            int updated = session.createQuery(
                            "update ChatConversationParticipant p set p.lastReadAt = :now " +
                                    "where p.conversation.id = :conversationId and p.user.userId = :userId")
                    .setParameter("now", new Timestamp(System.currentTimeMillis()))
                    .setParameter("conversationId", conversationId)
                    .setParameter("userId", userId)
                    .executeUpdate();
            if (updated == 0) {
                trans.rollback();
                return GeneralConstants.ENTRY_NOT_EXISTS;
            }
            trans.commit();
            return GeneralConstants.SUCCESSFUL;
        } catch (Exception e) {
            rollback(trans);
            logger.error("Unable to mark chat conversation {} as read for user {}", conversationId, userId, e);
            return GeneralConstants.FAILED;
        } finally {
            close(session);
        }
    }

    private void rollback(Transaction trans) {
        if (trans != null) {
            trans.rollback();
        }
    }

    private void close(Session session) {
        if (session != null) {
            session.close();
        }
    }
}
