package com.sammedsp.fintrack.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;

@Entity(name = "folders")
public class Folder extends BaseEntity{

    private String name;

    @Column(name = "user_id")
    private String userId;

    @Column(name = "is_shared")
    private boolean isShared = false;

    public boolean isShared() {
        return isShared;
    }

    public void setShared(boolean shared) {
        isShared = shared;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
