package com.app.chaton;

import android.content.Intent;
import android.graphics.Typeface;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.app.chaton.API_helpers.CallService;
import com.app.chaton.API_helpers.RequestHelper;
import com.app.chaton.API_helpers.RequestObject;
import com.app.chaton.API_helpers.ResponseObject;
import com.app.chaton.API_helpers.ServiceGenerator;
import com.app.chaton.Utils.PreferenceHelper;
import com.app.chaton.org.java_websocket.client.DefaultSSLWebSocketClientFactory;
import com.app.chaton.org.java_websocket.client.WebSocketClient;
import com.app.chaton.org.java_websocket.drafts.Draft_17;
import com.app.chaton.org.java_websocket.handshake.ServerHandshake;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.security.ProviderInstaller;

import java.net.URI;
import java.security.GeneralSecurityException;

import javax.net.ssl.SSLContext;

import retrofit2.Response;

import com.google.android.vending.licensing.AESObfuscator;
import com.google.android.vending.licensing.LicenseChecker;
import com.google.android.vending.licensing.LicenseCheckerCallback;
import com.google.android.vending.licensing.ServerManagedPolicy;

public class MainActivity extends AppCompatActivity {

    private static final String BASE64_PUBLIC_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAk14k+74REn5iwz6lS1olrtbDxajS+5bpufPdVJW/VLCS11LNQb9F30nJBMfR3KIp3mjEot8Pc9C9bh2vdeUQRxZRBJFdWpODYfSLsWx58L9T8ZDE8p3kzHKD1I+B3gCxyZT0Ephyifen5Wp9NBaZsaw1NJQ+Aqpz0WxF87jgw2uA6AWLnxfxHhiUS+TaEizD7CWuEx2dT3nRidTCRDccjGfl4SxMZ4EXRew2PhK3FMqDm+0JpRj89v1yXnmgqw+ngc7T+mJw1K8SQBCiRVds1w4Cpe0vpbGZg1awewmuEeW9OVXFtGiY9ySV634m2zwFFmmsB6c8RZ8UA0d+ZuwxIQIDAQAB";

    Button btnSignUp, btnLogIn;

    WebSocketClient client;
    CallService callService;
    PreferenceHelper preferenceHelper;

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

        setContentView(R.layout.start_screen);

        btnSignUp = (Button) findViewById(R.id.btnSignUp);
        btnLogIn = (Button) findViewById(R.id.btnLogIn);

        Typeface myriad = Typeface.createFromAsset(getAssets(), "fonts/MyriadPro.ttf");
        btnSignUp.setTypeface(myriad);
        btnLogIn.setTypeface(myriad);

        if (!preferenceHelper.isAuth()) {

            btnSignUp.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // TODO
                }
            });
            btnLogIn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(MainActivity.this, AuthActivity.class);
                    startActivity(intent);
                }
            });
        } else {
            Log.d("myLogs", preferenceHelper.getSecretKey());

            btnLogIn.setVisibility(View.GONE);
            btnSignUp.setText(R.string.logOut);

            btnSignUp.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    preferenceHelper.reset();
                    Intent intent = getIntent();
                    finish();
                    startActivity(intent);
                }
            });

            Long id = preferenceHelper.getId();
            String sk = preferenceHelper.getSecretKey();

            RequestHelper requestHelper = new RequestHelper() {
                @Override
                public void onStatusOk(Response<ResponseObject> response) {
                    Log.d("myLogs", "ok");
                }

                @Override
                public void onStatusServerError(Response<ResponseObject> response) {
                    Log.d("myLogs", "server error");
                }

                @Override
                public void onFail(Throwable t) {
                    Log.d("myLogs", "fail");
                }
            };
            requestHelper.getDialogs(callService, new RequestObject(new Object(), id, sk), id, sk);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mChecker != null)
            mChecker.onDestroy();
    }

    private void connectToSocket() throws GeneralSecurityException,
            GooglePlayServicesRepairableException, GooglePlayServicesNotAvailableException{
        client = new WebSocketClient(URI.create(getResources().getString(R.string.SOCKET_URL)), new Draft_17()) {
            @Override
            public void onOpen(ServerHandshake handshakedata) {
                Log.d("myLogs", "opened");
                client.send("hey");
                client.close();
            }

            @Override
            public void onMessage(String message) {
                Log.d("myLogs", "mess " + message);
            }

            @Override
            public void onClose(int code, String reason, boolean remote) {
                Log.d("myLogs", "closed " + reason + " " + code + " " + remote);
            }

            @Override
            public void onError(Exception ex) {
                Log.d("myLogs", "error " + ex.toString());
                ex.printStackTrace();
            }
        };

        ProviderInstaller.installIfNeeded(getApplicationContext());
        SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
        sslContext.init(null, null, null);
        client.setWebSocketFactory(new DefaultSSLWebSocketClientFactory(sslContext));
        client.connect();

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
            Log.d("myLogs", "app error");

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
