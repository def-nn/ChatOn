package com.app.chaton.API_helpers;

import android.util.Log;

public class Message {
    public static final int TYPE_TO = 0;
    public static final int TYPE_FROM = 1;

    private Long id;
    private Long owner;
    private Long companion;
    private Long receiver;
    private String message;
    private Long viewed;
    private Long created_at;
    private User username;

    public long getId() { return this.id; }
    public User getCompanion() { return this.username; }

    public Long from() { return this.owner; }
    public Long to() { return this.receiver; }

    public String getBody() { return this.message; }
    public boolean isViewed() { return this.viewed != 0; }

    public long createdAt() { return this.created_at; }

    public int getType() {
        Log.d("myLogs", "type: " + companion + " " + receiver + " " + companion.equals(receiver));
        return ((companion.equals(receiver)) ? TYPE_TO : TYPE_FROM);
    }
}
