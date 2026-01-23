package com.example.shopifun.models;

import java.util.Date;

public class UserList {
    private String id;       // ID документа
    private String title;    // Название списка
    private Date createdAt;  // Дата создания

    public UserList() {
        // Пустой конструктор для Firestore
    }

    public UserList(String title, Date createdAt) {
        this.title = title;
        this.createdAt = createdAt;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
}
