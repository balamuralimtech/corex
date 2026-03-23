package com.persist.coretix.modal.coretix;

import javax.persistence.*;
import java.sql.Timestamp;

@Entity
@Table(name = "applicationtheme")
public class ApplicationTheme {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "userid", nullable = true)
    private Integer userId;

    @Column(name = "theme", length = 50)
    private String theme;

    @Column(name = "layout", length = 50)
    private String layout;

    @Column(name = "menuclass", length = 50)
    private String menuClass;

    @Column(name = "profilemode", length = 50)
    private String profileMode;

    @Column(name = "menulayout", length = 50)
    private String menuLayout;

    @Column(name = "inputstyle", length = 50)
    private String inputStyle;

    @Column(name = "created_at", nullable = false, updatable = false, insertable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private Timestamp createdAt;

    @Column(name = "updated_at", nullable = false, insertable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    private Timestamp updatedAt;

    // Getters and Setters

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getTheme() {
        return theme;
    }

    public void setTheme(String theme) {
        this.theme = theme;
    }

    public String getLayout() {
        return layout;
    }

    public void setLayout(String layout) {
        this.layout = layout;
    }

    public String getMenuClass() {
        return menuClass;
    }

    public void setMenuClass(String menuClass) {
        this.menuClass = menuClass;
    }

    public String getProfileMode() {
        return profileMode;
    }

    public void setProfileMode(String profileMode) {
        this.profileMode = profileMode;
    }

    public String getMenuLayout() {
        return menuLayout;
    }

    public void setMenuLayout(String menuLayout) {
        this.menuLayout = menuLayout;
    }

    public String getInputStyle() {
        return inputStyle;
    }

    public void setInputStyle(String inputStyle) {
        this.inputStyle = inputStyle;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }
}

