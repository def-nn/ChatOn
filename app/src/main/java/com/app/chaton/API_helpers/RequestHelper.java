package com.app.chaton.API_helpers;


import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public abstract class RequestHelper {

    private static final String ACT_AUTH = "auth";
    private static final String ACT_GET_DIALOGS = "getDialogs";
    private static final String ACT_GET_DIALOGS_BY_ID = "getDialogById";

    private final static int STATUS_OK = 200;
    private final static int STATUS_SERVER_ERROR = 500;

    private final static int STATUS_405 = 405;
    private final static int STATUS_406 = 406;

    private final static Gson gson;
    static { gson = new GsonBuilder().create(); }

    public void auth(CallService callService, RequestObject user) {
        Call<MapResponseObject> call = callService.auth(
                new String(Base64.encodeBase64(ACT_AUTH.getBytes())),
                new String(Base64.encodeBase64(new GsonBuilder().create().toJson(user).getBytes()))
        );

        call.enqueue(new Callback<MapResponseObject>() {
            @Override
            public void onResponse(Call<MapResponseObject> call, Response<MapResponseObject> response) {
                Log.d("myLogs", "body " + response.body().getData().toString());
                Log.d("myLogs", "code " + response.body().getStatus());
                switch (response.body().getStatus()) {
                    case STATUS_OK:
                        onStatusOk(response.body());
                        break;
                    case STATUS_SERVER_ERROR:
                        onStatusServerError(response.body());
                        break;
                    case STATUS_405:
                        onStatus405(response.body());
                        break;
                    case STATUS_406:
                        onStatus406(response.body());
                        break;
                }
            }

            @Override
            public void onFailure(Call<MapResponseObject> call, Throwable t) {
                Log.d("myLogs", "failure " + t.toString());
                onFail(t);
            }
        });
    }

    public void getDialogs(CallService callService, RequestObject obj, Long _u, String secret_key) {
        Call<MessageResponseObject> call = callService.getDialogs(
                _u,
                encode_s(secret_key),
                new String(Base64.encodeBase64(ACT_GET_DIALOGS.getBytes())),
                new String(Base64.encodeBase64(new GsonBuilder().create().toJson(obj).getBytes()))
        );

        call.enqueue(new Callback<MessageResponseObject>() {
            @Override
            public void onResponse(Call<MessageResponseObject> call, Response<MessageResponseObject> response) {
                switch (response.body().getStatus()) {
                    case STATUS_OK:
                        onStatusOk(response.body());
                        break;
                    case STATUS_SERVER_ERROR:
                        onStatusServerError(response.body());
                        break;
                }
            }

            @Override
            public void onFailure(Call<MessageResponseObject> call, Throwable t) {
                onFail(t);
            }
        });
    }

    public void getDialogsById(CallService callService, Long companionId, Long _u, String secret_key) {
        Call<MessageResponseObject> call = callService.getDialogs(
                _u,
                encode_s(secret_key),
                new String(Base64.encodeBase64(ACT_GET_DIALOGS_BY_ID.getBytes())),
                new String(Base64.encodeBase64(
                        new GsonBuilder().create().toJson(
                                new RequestObject(
                                    new User(companionId), _u, secret_key)
                                ).getBytes())
                )
        );

        call.enqueue(new Callback<MessageResponseObject>() {
            @Override
            public void onResponse(Call<MessageResponseObject> call, Response<MessageResponseObject> response) {
                switch (response.body().getStatus()) {
                    case STATUS_OK:
                        onStatusOk(response.body());
                        break;
                    case STATUS_SERVER_ERROR:
                        onStatusServerError(response.body());
                        break;
                }
            }

            @Override
            public void onFailure(Call<MessageResponseObject> call, Throwable t) {
                onFail(t);
            }
        });
    }

    public static boolean isConnected(Context context) {
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

    public static String encode_s(String data) {
        byte[] base64 = Base64.encodeBase64(data.getBytes());
        String sha1 = new String(Hex.encodeHex(DigestUtils.sha1(base64)));
        return new String(Hex.encodeHex(DigestUtils.md5(sha1)));
    }

    public void onStatusOk(MapResponseObject response) {};
    public void onStatusOk(MessageResponseObject response) {};
    public void onStatusServerError(MapResponseObject response) {};
    public void onStatusServerError(MessageResponseObject response) {};
    public void onFail(Throwable t) {};

    // Обработка данных ответов не всегда должна быть реализована
    public void onStatus405(MapResponseObject response) {};
    public void onStatus406(MapResponseObject response) {};
}
