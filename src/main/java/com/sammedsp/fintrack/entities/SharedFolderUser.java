package com.sammedsp.fintrack.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;

@Entity(name = "shared_folder_users")
public class SharedFolderUser extends BaseEntity {
    @Column(name = "folder_id")
    private String folderId;

    @Column(name = "user_id")
    private String userId;

    public String getFolderId() {
        return folderId;
    }

    public void setFolderId(String folderId) {
        this.folderId = folderId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
