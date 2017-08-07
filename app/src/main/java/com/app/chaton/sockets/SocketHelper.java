package com.app.chaton.sockets;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.app.chaton.API_helpers.Message;
import com.app.chaton.API_helpers.RequestHelper;
import com.app.chaton.org.java_websocket.client.DefaultSSLWebSocketClientFactory;
import com.app.chaton.org.java_websocket.client.WebSocketClient;
import com.app.chaton.org.java_websocket.drafts.Draft;
import com.app.chaton.org.java_websocket.drafts.Draft_17;
import com.app.chaton.org.java_websocket.exceptions.WebsocketNotConnectedException;
import com.app.chaton.org.java_websocket.handshake.ServerHandshake;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.security.ProviderInstaller;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.net.URI;
import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.net.ssl.SSLContext;

public class SocketHelper {

    private static final String SOCKET_URL = "wss://chaton.ga/wss2/NNN:8080";

    private static final String _U = "_u";
    private static final String _S = "_s";
    private static final String ACT = "act";
    private static final String U_ID = "uid";
    private static final String MESSAGE = "message";
    private static final String TEMP_ID = "temp_id";
    private static final String DATA = "data";
    private static final String SENDER = "sender";
    private static final String RECEIVER = "receiver";
    private static final String ID = "id";
    private static final String CREATED_AT = "created_at";

    private static final String ACT_MESSAGE = "message";
    private static final String ACT_TYPING = "type_pending";

    private Context context;
    private Long id;
    private Map<String, String> headers;
    private SocketClient client;

    private SocketListener socketListener;

    private Set<Long> messageStack;

    private class SocketClient extends WebSocketClient {

        private SocketClient(URI serverUri, Draft draft, Map<String, String> headers, int connecttimeout) {
            super(serverUri, draft, headers, connecttimeout);
        }

        @Override
        public void onOpen(ServerHandshake handshakedata) {
            Log.d("myLogs", "opened");
        }

        @Override
        public void onMessage(String message) {
            Log.d("myLogs", "mess " + message);
            JsonObject jsonObject = new JsonParser().parse(message).getAsJsonObject();

            switch (jsonObject.get(ACT).getAsString()) {
                case ACT_MESSAGE:
                    JsonObject data = jsonObject.get(DATA).getAsJsonObject();

                    if (data.get(SENDER).getAsJsonObject().get(ID).getAsLong() == socketListener.getCompanionId() ||
                            data.get(RECEIVER).getAsJsonObject().get(ID).getAsLong() == socketListener.getCompanionId()) {
                        HashMap<String, Object> mess_data = new HashMap<>();
                        mess_data.put(Message.ID, data.get(ID).getAsLong());
                        mess_data.put(Message.OWNER, data.get(SENDER).getAsJsonObject().get(ID).getAsLong());
                        mess_data.put(Message.RECEIVER, data.get(RECEIVER).getAsJsonObject().get(ID).getAsLong());
                        mess_data.put(Message.MESSAGE, data.get(MESSAGE).getAsString());
                        mess_data.put(Message.CREATED_AT, data.get(CREATED_AT).getAsLong());
                        try {
                            mess_data.put(Message.TEMP_ID, jsonObject.get(TEMP_ID).getAsString());
                        } catch (UnsupportedOperationException e) {
                            mess_data.put(Message.TEMP_ID, null);
                        }
                        // TODO
                        Log.d("myLogs", "mess " + mess_data.toString());
                        socketListener.onMessageReceived(new Message(mess_data));
                    }

                    break;
                case ACT_TYPING:
                    if (jsonObject.get(SENDER).getAsLong() == socketListener.getCompanionId()) {
                        socketListener.setTyping(true);
                        socketListener.onTypePending();
                    }
                default:
                    Log.d("myLogs", jsonObject.get(ACT).getAsString());
            }
        }

        @Override
        public void onClose(int code, String reason, boolean remote) {
            Log.d("myLogs", "closed " + reason + " " + code + " " + remote);

            if (code == 1006 || code == 1002)
                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Log.d("myLogs", "reconnecting");
                        SocketHelper.this.reconnect();
                    }
                }, 200);
        }

        @Override
        public void onError(Exception ex) {
            Log.d("myLogs", "error " + ex.toString());
            ex.printStackTrace();
        }
    }

    public SocketHelper(Context context, Long _u, String _s) {
        this.context = context;
        this.id = _u;

        this.headers = new HashMap<>();
        headers.put(_U, _u.toString());
        headers.put(_S, RequestHelper.encode_s(_s));

        client = new SocketClient(URI.create(SOCKET_URL), new Draft_17(), headers, 0);

        this.messageStack = new HashSet<>();
    }

    public boolean hasNewMess() { return (messageStack.size() > 0); }

    public void removeMessFromStack(Long id) {
        messageStack.remove(id);
        Log.d("myLogs", "stack " + messageStack);
    }

    public boolean isConnected() { return client.getConnection().isOpen(); }

    public void connect() {
        try {
            ProviderInstaller.installIfNeeded(context);
            SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
            sslContext.init(null, null, null);
            client.setWebSocketFactory(new DefaultSSLWebSocketClientFactory(sslContext));
        } catch (GooglePlayServicesRepairableException |
                 GooglePlayServicesNotAvailableException |
                 GeneralSecurityException e) { e.printStackTrace(); }

        client.connect();
    }

    public void reconnect() {
        client.close();
        client = new SocketClient(URI.create(SOCKET_URL), new Draft_17(), headers, 0);
        connect();
    }

    public void send(Long receiver, String message) throws WebsocketNotConnectedException {
        if (!client.getConnection().isOpen()) reconnect();

        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty(ACT, ACT_MESSAGE);
        jsonObject.addProperty(U_ID, receiver);
        jsonObject.addProperty(MESSAGE, message);

        client.send(jsonObject.toString());
    }

    public void notifyTyping(Long receiverId) throws WebsocketNotConnectedException {
        if (!client.getConnection().isOpen()) reconnect();

        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty(ACT, ACT_TYPING);
        jsonObject.addProperty(RECEIVER, receiverId);

        client.send(jsonObject.toString());
    }

    public void setSocketListener(SocketListener socketListener) {
        this.socketListener = socketListener;
    }
}
