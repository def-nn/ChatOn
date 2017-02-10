package com.app.chaton;

import android.content.SharedPreferences;
import android.util.Log;

import com.app.chaton.API_helpers.User;

public class PreferenceHelper {

    private final static String IS_AUTH = "is_auth";
    private final static String NAME = "name";
    private final static String EMAIL = "email";
    private final static String IS_ADMIN = "is_admin";
    private final static String SECRET_KEY = "secret_key";
    private final static String ID = "id";

    private SharedPreferences preferences;

    PreferenceHelper(SharedPreferences preferences) {
        this.preferences = preferences;
    }

    public boolean isAuth() {
        return preferences.getBoolean(IS_AUTH, false);
    }

    public void authUser(User user) {
        SharedPreferences.Editor editor = preferences.edit();

        editor.putBoolean(IS_AUTH, true);
        editor.putString(NAME, user.getName());
        editor.putString(EMAIL, user.getEmail());
        editor.putBoolean(IS_ADMIN, user.isAdmin());
        editor.putString(SECRET_KEY, user.getSecretKey());
        editor.putLong(ID, user.getId());

        editor.apply();
    }

    public String getName() {
        return preferences.getString(NAME, "");
    }

    public String getSecretKey() {
        return preferences.getString(SECRET_KEY, "");
    }

    public Long getId() {
        return preferences.getLong(ID, 0);
    }
}
