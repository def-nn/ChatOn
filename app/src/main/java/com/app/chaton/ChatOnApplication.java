package com.app.chaton;

import android.app.Application;

import com.app.chaton.Utils.ToastHelper;

public class ChatOnApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        ToastHelper.createToast(getApplicationContext());
    }
}
