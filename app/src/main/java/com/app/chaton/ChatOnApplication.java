package com.app.chaton;

import android.app.Application;
import android.support.multidex.MultiDexApplication;

import com.app.chaton.Utils.ToastHelper;

public class ChatOnApplication extends MultiDexApplication {

    @Override
    public void onCreate() {
        super.onCreate();

        ToastHelper.createToast(getApplicationContext());
    }
}
