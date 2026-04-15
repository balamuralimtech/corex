package com.persist.coretix.modal.coretix.dto;

import java.io.Serializable;

public class ChatContactSummary implements Serializable {

    private final int userId;
    private final String userName;
    private final String userType;
    private final Integer organizationId;
    private final String organizationName;

    public ChatContactSummary(int userId, String userName, String userType, Integer organizationId, String organizationName) {
        this.userId = userId;
        this.userName = userName;
        this.userType = userType;
        this.organizationId = organizationId;
        this.organizationName = organizationName;
    }

    public int getUserId() {
        return userId;
    }

    public String getUserName() {
        return userName;
    }

    public String getUserType() {
        return userType;
    }

    public Integer getOrganizationId() {
        return organizationId;
    }

    public String getOrganizationName() {
        return organizationName;
    }

    public String getDisplayLabel() {
        if (organizationName == null || organizationName.trim().isEmpty()) {
            return userName;
        }
        return userName + " (" + organizationName + ")";
    }
}
