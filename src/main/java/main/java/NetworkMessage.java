package main.java;

import java.io.Serializable;

public class NetworkMessage implements Serializable {
    private static final long serialVersionUID = 1L;

    public enum MessageType {
        CONNECT,
        DISCONNECT,
        MOVE,
        GAME_STATE,
        CHAT,
        ERROR
    }

    private MessageType type;
    private Object data;
    private String senderId;
    private long timestamp;

    public NetworkMessage(MessageType type, Object data) {
        this.type = type;
        this.data = data;
        this.timestamp = System.currentTimeMillis();
    }

    public MessageType getType() { return type; }
    public Object getData() { return data; }
    public String getSenderId() { return senderId; }
    public void setSenderId(String senderId) { this.senderId = senderId; }
    public long getTimestamp() { return timestamp; }
}