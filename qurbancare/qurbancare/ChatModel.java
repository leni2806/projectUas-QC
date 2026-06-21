package com.example.qurbancare;

public class ChatModel {
    public static String SENT_BY_ME = "me";
    public static String SENT_BY_BOT = "bot";

    String message;
    String sentBy;

    public ChatModel(String message, String sentBy) {
        this.message = message;
        this.sentBy = sentBy;
    }
}