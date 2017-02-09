package com.app.chaton;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.app.chaton.WebSockets.WebSocketClient;

import org.apache.http.message.BasicNameValuePair;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity {

    WebSocketClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btnSignIn = (Button) findViewById(R.id.btnSignIn);
        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, AuthActivity.class);
                startActivity(intent);
            }
        });

        connectToSocket();
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
