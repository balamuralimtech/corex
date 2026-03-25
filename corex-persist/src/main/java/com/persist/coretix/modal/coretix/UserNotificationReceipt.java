package com.persist.coretix.modal.coretix;

import com.persist.coretix.modal.usermanagement.UserDetails;

import javax.persistence.*;
import java.sql.Timestamp;

@Entity
@Table(name = "user_notification_receipt",
        uniqueConstraints = @UniqueConstraint(name = "uk_user_notification_receipt", columnNames = {"notification_id", "user_id"}))
public class UserNotificationReceipt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "notification_id", nullable = false)
    private ApplicationNotification notification;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserDetails user;

    @Column(name = "seen_at", nullable = false, insertable = false,
            columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private Timestamp seenAt;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public ApplicationNotification getNotification() {
        return notification;
    }

    public void setNotification(ApplicationNotification notification) {
        this.notification = notification;
    }

    public UserDetails getUser() {
        return user;
    }

    public void setUser(UserDetails user) {
        this.user = user;
    }

    public Timestamp getSeenAt() {
        return seenAt;
    }

    public void setSeenAt(Timestamp seenAt) {
        this.seenAt = seenAt;
    }
}
