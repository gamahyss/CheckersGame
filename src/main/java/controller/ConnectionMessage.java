package controller;

public class ConnectionMessage {
    private String username;
    private boolean connected;
    private boolean isHost;

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public boolean isConnected() { return connected; }
    public void setConnected(boolean connected) { this.connected = connected; }

    public boolean isHost() { return isHost; }
    public void setHost(boolean host) { isHost = host; }
}