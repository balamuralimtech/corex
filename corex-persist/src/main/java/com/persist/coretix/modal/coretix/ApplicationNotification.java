package com.persist.coretix.modal.coretix;

import javax.persistence.*;
import java.sql.Timestamp;

@Entity
@Table(name = "application_notification")
public class ApplicationNotification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "message", nullable = false, length = 1000)
    private String message;

    @Column(name = "created_by_user_id")
    private Integer createdByUserId;

    @Column(name = "created_by_user_name", length = 100)
    private String createdByUserName;

    @Column(name = "created_at", nullable = false, updatable = false, insertable = false,
            columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private Timestamp createdAt;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Integer getCreatedByUserId() {
        return createdByUserId;
    }

    public void setCreatedByUserId(Integer createdByUserId) {
        this.createdByUserId = createdByUserId;
    }

    public String getCreatedByUserName() {
        return createdByUserName;
    }

    public void setCreatedByUserName(String createdByUserName) {
        this.createdByUserName = createdByUserName;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }
}
