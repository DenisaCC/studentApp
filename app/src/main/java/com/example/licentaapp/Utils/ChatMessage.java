package com.example.licentaapp.Utils;

import java.util.HashMap;
import java.util.Map;

public class ChatMessage {
    private String id;
    private String userId;
    private String userName;
    private String text;
    private String receiverId;

    public ChatMessage() {
        // Constructorul gol necesar pentru Firebase
    }

    public ChatMessage(String id, String userId, String userName, String text, String receiverId) {
        this.id = id;
        this.userId = userId;
        this.userName = userName;
        this.text = text;
        this.receiverId = receiverId;
    }

    public String getId() {
        return id;
    }

    public String getUserId() {
        return userId;
    }

    public String getUserName() {
        return userName;
    }

    public String getText() {
        return text;
    }

    public String getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(String receiverId) {
        this.receiverId = receiverId;
    }

    // Convertim un obiect ChatMessage într-un Map pentru a-l salva în baza de date Firebase
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("id", id);
        result.put("userId", userId);
        result.put("userName", userName);
        result.put("text", text);
        result.put("receiverId", receiverId);
        return result;
    }
}
