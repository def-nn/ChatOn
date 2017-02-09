package com.app.chaton.API_helpers;


import java.util.HashMap;

public class User {

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
    private Long _u;

    public User(String email, String password) {
        this.email = email;
        this.password = password;
    }

    public User(HashMap<String, String> data) {
        this.name = data.get(NAME);
        this.email = data.get(EMAIL);
        this.is_admin = Boolean.valueOf(data.get(IS_ADMIN));
        this.secret_key = data.get(SECRET_KEY);
        this._u = Long.valueOf(data.get(_U));
    }

    public String getName() { return this.name; }
    public String getEmail() { return this.email; }
    public String getSecretKey() { return this.secret_key; }
    public Long getId() { return this._u; }

}
