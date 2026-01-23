package com.example.shopifun.models;

public class ShoppingItem {
    private String id;
    private String title;
    private String note;
    private boolean completed;

    public ShoppingItem() {
        // Пустой конструктор для Firestore
    }

    public ShoppingItem(String title, String note, boolean completed) {
        this.title = title;
        this.note = note;
        this.completed = completed;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean completed) { this.completed = completed; }
}
