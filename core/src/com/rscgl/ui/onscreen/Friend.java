package com.rscgl.ui.onscreen;

public class Friend {

    public Friend(boolean online, String username) {
        this.online = online;
        this.username = username;
    }

    private boolean online;
    private String username;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public boolean isOnline() {
        return online;
    }

    public void setOnline(boolean online) {
        this.online = online;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof Friend)
            return ((Friend) obj).username.equals(username);

        return super.equals(obj);
    }
}
