package com.app.chaton.API_helpers;


import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;

import java.util.HashMap;

public class User implements Parcelable{

    private final static String NAME = "name";
    private final static String EMAIL = "email";
    private final static String IS_ADMIN = "is_admin";
    private final static String SECRET_KEY = "secret_key";
    private final static String _U = "_u";

    private String name;
    private String email;
    private String password;
    private Boolean is_admin;
    private String secret_key;
    private Long _u, id;
    private Long last_online;
    private String avatar;

    // Используется при отправке запроса на авторизацию пользователя
    public User(String email, @Nullable String password, @Nullable String secret_key) {
        this.email = email;
        if (password != null) this.password = password;
        else this.secret_key = secret_key;
    }

    // Используется при получении ответа от сервера (в виде ассоциативного массива)
    public User(HashMap<String, String> data) {
        this.name = data.get(NAME);
        this.email = data.get(EMAIL);
        this.is_admin = Boolean.valueOf(data.get(IS_ADMIN));
        this.secret_key = data.get(SECRET_KEY);
        this._u = Long.valueOf(data.get(_U));
    }

    // Используется при отправке запроса с идентефикатором пользователя
    public User(Long id) {
        this.id = id;
    }

    public Long getId() {
        return (this._u == null) ? this.id : this._u;
    }

    public String getName() { return this.name; }
    public String getEmail() { return this.email; }
    public String getSecretKey() { return this.secret_key; }
    public String getAvatar() { return this.avatar; }
    public boolean isAdmin() { return this.is_admin; }
    public long when_last_online() { return this.last_online; }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(name);
        parcel.writeString(email);
        parcel.writeString(password);
        parcel.writeByte((byte) (is_admin ? 1 : 0));
        parcel.writeString(secret_key);
        parcel.writeLong((_u != null) ? _u : 0);
        parcel.writeLong(id);
        parcel.writeLong(last_online);
    }

    private User(Parcel parcel) {
        name = parcel.readString();
        email = parcel.readString();
        password = parcel.readString();
        is_admin = parcel.readByte() != 0;
        secret_key = parcel.readString();
        _u = parcel.readLong();
        id = parcel.readLong();
        last_online = parcel.readLong();
    }

    public static final Creator<User> CREATOR = new Creator<User>() {
        @Override
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };
}
