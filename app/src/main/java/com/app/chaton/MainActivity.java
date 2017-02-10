package com.app.chaton;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.app.chaton.API_helpers.CallService;
import com.app.chaton.API_helpers.RequestHelper;
import com.app.chaton.API_helpers.RequestObject;
import com.app.chaton.API_helpers.ResponseObject;
import com.app.chaton.API_helpers.ServiceGenerator;
import com.app.chaton.WebSockets.WebSocketClient;

import org.apache.http.message.BasicNameValuePair;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Response;


public class MainActivity extends AppCompatActivity {

    Button btnSignIn, btnLogOut;

    CallService callService;
    WebSocketClient client;
    PreferenceHelper preferenceHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnSignIn = (Button) findViewById(R.id.btnSignIn);
        btnLogOut = (Button) findViewById(R.id.btnLogOut);

        callService = ServiceGenerator.createService(CallService.class);
        preferenceHelper = new PreferenceHelper(getSharedPreferences(
                getResources().getString(R.string.PREFERENCE_FILE), MODE_PRIVATE));

        if (preferenceHelper.isAuth()) {

            Log.d("myLogs", "ok " + preferenceHelper.getSecretKey());

            ((TextView) findViewById(R.id.tv)).setText("Hello " + preferenceHelper.getName());
            btnSignIn.setText("Your id " + preferenceHelper.getId());
            btnSignIn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Toast.makeText(getApplicationContext(), preferenceHelper.getSecretKey(), Toast.LENGTH_SHORT).show();

                    RequestHelper requestHelper = new RequestHelper() {
                        @Override
                        public void onStatusOk(Response<ResponseObject> response) {
                            Log.d("myLogs", "ok " + response.body().getData().toString());
                        }

                        @Override
                        public void onStatusServerError(Response<ResponseObject> response) {
                            Log.d("myLogs", "server error");
                        }

                        @Override
                        public void onStatus405(Response<ResponseObject> response) {}
                        @Override
                        public void onStatus406(Response<ResponseObject> response) {}

                        @Override
                        public void onFail(Throwable t) {
                            Log.d("myLogs", "fail");
                        }
                    };

                    requestHelper.getDialogs(
                            callService,
                            new RequestObject(new Object(), preferenceHelper.getId(), preferenceHelper.getSecretKey()),
                            preferenceHelper.getId(),
                            preferenceHelper.getSecretKey()
                    );
                }
            });

            btnLogOut.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    preferenceHelper.reset();

                    Intent restartIntent = getIntent();
                    finish();
                    startActivity(restartIntent);
                }
            });
        } else {
            btnSignIn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(MainActivity.this, AuthActivity.class);
                    startActivity(intent);
                }
            });
        }
    }

    @Override
    protected void onStart() {
        super.onRestart();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void connectToSocket() {
        List<BasicNameValuePair> extraHeaders = new ArrayList<>();
        URI uri = URI.create(getResources().getString(R.string.SOCKET_URL));

        client = new WebSocketClient(uri, new WebSocketClient.Listener() {
            @Override
            public void onConnect() {
                Log.d("myLogs", "connected");
            }

            @Override
            public void onMessage(String message) {
                Log.d("myLogs", "mess " + message);
            }

            @Override
            public void onMessage(byte[] data) {
                Log.d("myLogs", "mess");
            }

            @Override
            public void onDisconnect(int code, String reason) {
                Log.d("myLogs", "disconnected " + reason + code);
            }

            @Override
            public void onError(Exception error) {
                Log.d("myLogs", "error");
            }
        }, extraHeaders);

        client.connect();
        client.disconnect();
    }

}
