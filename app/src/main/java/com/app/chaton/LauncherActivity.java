package com.app.chaton;

import android.app.Activity;
import android.content.Intent;
import android.provider.Settings;
import android.os.Bundle;
import android.util.Log;

import com.app.chaton.API_helpers.CallService;
import com.app.chaton.API_helpers.MapResponseObject;
import com.app.chaton.API_helpers.RequestHelper;
import com.app.chaton.API_helpers.RequestObject;
import com.app.chaton.API_helpers.ServiceGenerator;
import com.app.chaton.API_helpers.User;
import com.app.chaton.Utils.ImageDownloader;
import com.app.chaton.Utils.PreferenceHelper;

import com.google.android.vending.licensing.AESObfuscator;
import com.google.android.vending.licensing.LicenseChecker;
import com.google.android.vending.licensing.LicenseCheckerCallback;
import com.google.android.vending.licensing.ServerManagedPolicy;

import java.io.IOException;


public class LauncherActivity extends Activity {

    private static final String BASE64_PUBLIC_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAk14k+74REn5iwz6lS1olrtbDxajS+5bpufPdVJW/VLCS11LNQb9F30nJBMfR3KIp3mjEot8Pc9C9bh2vdeUQRxZRBJFdWpODYfSLsWx58L9T8ZDE8p3kzHKD1I+B3gCxyZT0Ephyifen5Wp9NBaZsaw1NJQ+Aqpz0WxF87jgw2uA6AWLnxfxHhiUS+TaEizD7CWuEx2dT3nRidTCRDccjGfl4SxMZ4EXRew2PhK3FMqDm+0JpRj89v1yXnmgqw+ngc7T+mJw1K8SQBCiRVds1w4Cpe0vpbGZg1awewmuEeW9OVXFtGiY9ySV634m2zwFFmmsB6c8RZ8UA0d+ZuwxIQIDAQAB";

    private PreferenceHelper preferenceHelper;
    private CallService callService;
    private RequestHelper helper;
    private LicenseCheckerCallback mLicenseCheckerCallback;
    private LicenseChecker mChecker;

    private static final byte[] SALT = new byte[] {
            13, -8, 56, 72, 4, -11, 121, 85, -67, -2, 24, 93, 17, -88, 119, 44, 32, -16, 98, 54
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        checkForLicense();

        callService = ServiceGenerator.createService(CallService.class);
        preferenceHelper = new PreferenceHelper(getSharedPreferences(
                getResources().getString(R.string.PREFERENCE_FILE), MODE_PRIVATE));

        if (preferenceHelper.isAuth()) authUser();
        else {
            Intent intent = new Intent(LauncherActivity.this, StartActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }
    }

    private void authUser() {
        final User user = new User(preferenceHelper.getEmail(), null, preferenceHelper.getSecretKey());

        helper = new RequestHelper() {
            @Override
            public void onStatusOk(MapResponseObject response) {
                Log.d("myLogs", response.getData().toString());
                User user = new User(response.getData());
                preferenceHelper.authUser(user);

                new ImageDownloader(getApplicationContext(), user.getAvatar()).execute();

                ((WeTuneApplication) getApplication()).connectToSocket(preferenceHelper.getId(),
                        preferenceHelper.getSecretKey());

                Intent intent = new Intent(LauncherActivity.this, DialogActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intent.putExtra(DialogActivity.AUTH_REQUEST_SENT, true);
                startActivity(intent);
            }

            @Override
            public void onStatusServerError(MapResponseObject response) {
                Log.d("myLogs", response.getData().toString());
                preferenceHelper.reset();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Intent intent = new Intent(LauncherActivity.this, StartActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    }
                });
            }

            @Override
            public void onStatus405(MapResponseObject response) { onStatusServerError(response); }

            @Override
            public void onStatus406(MapResponseObject response) { onStatusServerError(response); }

            @Override
            public void onFail(final Throwable t) {
                try {
                    throw t;
                } catch (IOException e) {
                    Intent intent = new Intent(LauncherActivity.this, DialogActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    intent.putExtra(DialogActivity.AUTH_REQUEST_SENT, false);
                    startActivity(intent);
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        };
        helper.auth(callService, new RequestObject(user));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mChecker != null)
            mChecker.onDestroy();
    }

    private class MyLicenseCheckerCallback implements LicenseCheckerCallback {
        public void allow(int reason) {
            Log.d("myLogs", "allow");
            if (isFinishing()) {
                return;
            }
        }

        public void dontAllow(int reason) {
            Log.d("myLogs", "don't allow");
            if (isFinishing()) {
                return;
            }
        }

        @Override
        public void applicationError(int errorCode) {
            Log.d("myLogs", "app error " + errorCode );

        }
    }

    private void checkForLicense() {
        String deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        mLicenseCheckerCallback = new MyLicenseCheckerCallback();
        mChecker = new LicenseChecker(
                this,
                new ServerManagedPolicy(this, new AESObfuscator(SALT, getPackageName(), deviceId)),
                BASE64_PUBLIC_KEY
        );

        mChecker.checkAccess(mLicenseCheckerCallback);
    }


}
