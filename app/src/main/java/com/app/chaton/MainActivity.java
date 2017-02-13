package com.app.chaton;

import android.content.Intent;
import android.graphics.Typeface;
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


public class MainActivity extends AppCompatActivity {

    Button btnSignUp, btnLogIn;

    WebSocketClient client;
    CallService callService;
    PreferenceHelper preferenceHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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
    protected void onStop() {
        super.onStop();
        client.close();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
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
}
