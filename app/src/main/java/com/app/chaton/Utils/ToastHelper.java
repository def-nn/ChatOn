package com.app.chaton.Utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.StringRes;
import android.widget.Toast;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class ToastHelper {
    private static Toast toast;

    @Retention(RetentionPolicy.SOURCE)
    private @interface Duration {}

    public static void makeToast(String text) {
        makeToast(text, Toast.LENGTH_SHORT);
    }

    public static void makeToast(@StringRes int resId) {
        makeToast(resId, Toast.LENGTH_SHORT);
    }

    public static void makeToast(String text, @Duration int duration) {
        toast.setText(text);
        toast.setDuration(duration);
        toast.show();
    }

    public static void makeToast(@StringRes int resId, @Duration int duration) {
        toast.setText(resId);
        toast.setDuration(duration);
        toast.show();
    }

    public static void createToast(Context context) {
        toast = Toast.makeText(context, "", Toast.LENGTH_SHORT);
    }
}
