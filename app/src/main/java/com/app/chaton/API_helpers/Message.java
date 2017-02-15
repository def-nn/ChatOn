package com.app.chaton.API_helpers;

public class Message {

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
}
