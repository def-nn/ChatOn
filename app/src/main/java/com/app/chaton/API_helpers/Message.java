package com.app.chaton.API_helpers;

import java.util.HashMap;

public class Message {

    private static final String ID = "id";
    private static final String OWNER = "owner";
    private static final String COMPANION = "companion";
    private static final String RECIEVER = "reciever";
    private static final String MESSAGE = "message";
    private static final String VIEWED = "viewed";
    private static final String CREATED_AT = "created_at";
    private static final String USERNAME = "username";

    private Long id;
    private Long owner;
    private Long companion;
    private Long receiver;
    private String message;
    private Long viewed;
    private Long created_at;
    private User username;

    Message(HashMap data) {
//        this.id = Long.valueOf(data.get(ID));
//        this.owner = Long.valueOf(data.get(OWNER));
//        this.companion = Long.valueOf(data.get(COMPANION));
//        this.receiver = Long.valueOf(data.get(RECIEVER));
//        this.message = data.get(MESSAGE);
//        this.viewed = data.get(VIEWED).equals("1");
//        this.created_at = Long.valueOf(data.get(CREATED_AT));
//        this.username = new User((HashMap<String, String>) data.get(USERNAME));

    }

    public long getId() { return this.id; }
//    public String getCompanion() { return this.username; }

    public Long from() { return this.owner; }
    public Long to() { return this.receiver; }

    public String getBody() { return this.message; }
    public boolean isViewed() { return this.viewed != 0; }

    public long createdAt() { return this.created_at; }
}
