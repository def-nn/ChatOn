package com.app.chaton.API_helpers;

import android.os.Parcel;
import android.os.Parcelable;

public class Message implements Parcelable{
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
        return ((companion.equals(receiver)) ? TYPE_TO : TYPE_FROM);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeLong(id);
        parcel.writeLong(owner);
        parcel.writeLong(companion);
        parcel.writeLong(receiver);
        parcel.writeString(message);
        parcel.writeLong(viewed);
        parcel.writeLong(created_at);
        parcel.writeParcelable(username, PARCELABLE_WRITE_RETURN_VALUE);
    }

    protected Message(Parcel parcel) {
        this.id = parcel.readLong();
        this.owner = parcel.readLong();
        this.companion = parcel.readLong();
        this.receiver = parcel.readLong();
        this.message = parcel.readString();
        this.viewed = parcel.readLong();
        this.created_at = parcel.readLong();
        this.username = parcel.readParcelable(getClass().getClassLoader());
    }

    public static final Creator<Message> CREATOR = new Creator<Message>() {
        @Override
        public Message createFromParcel(Parcel in) {
            return new Message(in);
        }

        @Override
        public Message[] newArray(int size) {
            return new Message[size];
        }
    };
}
