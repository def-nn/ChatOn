package com.app.chaton.API_helpers;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.HashMap;

public class Message{
    public static final int TYPE_TO = 0;
    public static final int TYPE_FROM = 1;

    public static final int STATE_PROCESS = 0;
    public static final int STATE_SUCCESS = 1;
    public static final int STATE_FAILURE = 2;

    public static final String ID = "id";
    public static final String OWNER = "owner";
    public static final String COMPANION = "companion";
    public static final String RECEIVER = "receiver";
    public static final String MESSAGE = "message";
    public static final String VIEWED = "viewed";
    public static final String CREATED_AT = "created_at";
    public static final String USERNAME = "username";
    public static final String TYPE = "type";
    public static final String STATE = "state";
    public static final String TEMP_ID = "temp_id";

    private Long id;
    private Long owner;
    private Long companion;
    private Long receiver;
    private String message;
    private Integer viewed;
    private Long created_at;
    private User username;
    private int state;
    private String temp_id;

    public Message(HashMap<String, Object> data) {
        this.id = (Long) data.get(ID);
        this.owner = (Long) data.get(OWNER);
        this.receiver = (Long) data.get(RECEIVER);
        this.message = (String) data.get(MESSAGE);
        this.viewed = 0;
        this.created_at = (Long) data.get(CREATED_AT);
        this.temp_id = (data.containsKey(TEMP_ID)) ?
                (String) data.get(TEMP_ID) :
                Message.createTempId(this.id, companion, created_at);
        // TODO
    }

    public void setCompanion(long companion) { this.companion = companion; }
    public void setCreatedAt(long created_at) { this.created_at = created_at; }
    public void setState(int state) { this.state = state; }

    public long getId() { return this.id; }
    public String getTempId() { return this.temp_id; }
    public User getCompanion() { return this.username; }

    public Long from() { return this.owner; }
    public Long to() { return this.receiver; }

    public String getBody() { return this.message.trim(); }

    public boolean isViewed() { return (this.viewed != null && this.viewed != 0); }
    public int getViewed() { return this.viewed; }

    public long createdAt() { return this.created_at; }

    public int getType() {
        return ((companion.equals(receiver)) ? TYPE_TO : TYPE_FROM);
    }

    public int getState() { return this.state; }

    public static String createTempId(Long id, Long companionId, Long created_at) {
        return RequestHelper.encode_s("temp_id" + id + companionId + created_at);
    }
}
