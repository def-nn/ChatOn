package com.app.chaton;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.app.chaton.WebSockets.WebSocketClient;

import org.apache.http.message.BasicNameValuePair;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity {

    Button btnSignIn;

    WebSocketClient client;
    PreferenceHelper preferenceHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnSignIn = (Button) findViewById(R.id.btnSignIn);
        preferenceHelper = new PreferenceHelper(getSharedPreferences(
                getResources().getString(R.string.PREFERENCE_FILE), MODE_PRIVATE));

        Log.d("myLogs", "" + preferenceHelper.isAuth());
        if (preferenceHelper.isAuth()) {

            ((TextView) findViewById(R.id.tv)).setText("Hello " + preferenceHelper.getName());
            btnSignIn.setText("Your id " + preferenceHelper.getId());
            btnSignIn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Toast.makeText(getApplicationContext(), preferenceHelper.getSecretKey(), Toast.LENGTH_SHORT).show();
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
        client.disconnect();
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
