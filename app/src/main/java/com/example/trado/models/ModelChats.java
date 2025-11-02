package com.example.trado.models;

public class ModelChats {
    private String profileImageUrl, name, chatKey, receiptUid, messageId, messageType, message, fromUid, toUid, lastMessage;
    private long timestamp;

    public ModelChats() {}

    public ModelChats(String messageId, String messageType, String message, String fromUid, String toUid, long timestamp) {
        this.messageId = messageId;
        this.messageType = messageType;
        this.message = message;
        this.fromUid = fromUid;
        this.toUid = toUid;
        this.timestamp = timestamp;
    }

    public ModelChats(String profileImageUrl, String name, String chatKey, String receiptUid,
                      String messageId, String messageType, String message, String fromUid,
                      String toUid, long timestamp, String lastMessage) {
        this.profileImageUrl = profileImageUrl;
        this.name = name;
        this.chatKey = chatKey;
        this.receiptUid = receiptUid;
        this.messageId = messageId;
        this.messageType = messageType;
        this.message = message;
        this.fromUid = fromUid;
        this.toUid = toUid;
        this.timestamp = timestamp;
        this.lastMessage = lastMessage;
    }

    public String getProfileImageUrl() { return profileImageUrl; }
    public void setProfileImageUrl(String profileImageUrl) { this.profileImageUrl = profileImageUrl; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getChatKey() { return chatKey; }
    public void setChatKey(String chatKey) { this.chatKey = chatKey; }
    public String getReceiptUid() { return receiptUid; }
    public void setReceiptUid(String receiptUid) { this.receiptUid = receiptUid; }
    public String getMessageId() { return messageId; }
    public void setMessageId(String messageId) { this.messageId = messageId; }
    public String getMessageType() { return messageType; }
    public void setMessageType(String messageType) { this.messageType = messageType; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getFromUid() { return fromUid; }
    public void setFromUid(String fromUid) { this.fromUid = fromUid; }
    public String getToUid() { return toUid; }
    public void setToUid(String toUid) { this.toUid = toUid; }
    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public String getReceiverUid() { return toUid; }

    public String getLastMessage() { return lastMessage; }
    public void setLastMessage(String lastMessage) { this.lastMessage = lastMessage; }
}
