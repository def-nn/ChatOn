package com.app.chaton.API_helpers;


import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public abstract class RequestHelper {

    private static final String ACT_AUTH = "auth";
    private static final String ACT_GET_DIALOGS = "getDialogs";

    private final static int STATUS_OK = 200;
    private final static int STATUS_SERVER_ERROR = 500;

    private final static int STATUS_405 = 405;
    private final static int STATUS_406 = 406;

    private final static Gson gson;
    static { gson = new GsonBuilder().create(); }

    public void auth(CallService callService, RequestObject user) {
        Call<ResponseObject> call = callService.auth(
                new String(Base64.encodeBase64(ACT_AUTH.getBytes())),
                new String(Base64.encodeBase64(new GsonBuilder().create().toJson(user).getBytes()))
        );

        call.enqueue(new Callback<ResponseObject>() {
            @Override
            public void onResponse(Call<ResponseObject> call, Response<ResponseObject> response) {
                switch (response.body().getStatus()) {
                    case STATUS_OK:
                        onStatusOk(response);
                        break;
                    case STATUS_SERVER_ERROR:
                        onStatusServerError(response);
                        break;
                    case STATUS_405:
                        onStatus405(response);
                        break;
                    case STATUS_406:
                        onStatus406(response);
                        break;
                }
            }

            @Override
            public void onFailure(Call<ResponseObject> call, Throwable t) {
                onFail(t);
            }
        });
    }

    public void getDialogs(CallService callService, RequestObject obj, Long _u, String secret_key) {
        Call<List<Map>> call = callService.getDialogs(
                _u.toString(),
                encode_s(secret_key),
                new String(Base64.encodeBase64(ACT_GET_DIALOGS.getBytes())),
                new String(Base64.encodeBase64(new GsonBuilder().create().toJson(obj).getBytes()))
        );

        call.enqueue(new Callback<List<Map>>() {
            @Override
            public void onResponse(Call<List<Map>> call, Response<List<Map>> response) {
//                switch (response.body().getStatus()) {
//                    case STATUS_OK:
//                        onStatusOk(response);
//                        break;
//                    case STATUS_SERVER_ERROR:
//                        onStatusServerError(response);
//                        break;
//                }
                Log.d("myLogs", "status: " + response.code());
                Log.d("myLogs", "message: " + response.message());
                Log.d("myLogs", "body: " + response.body().toString());
            }

            @Override
            public void onFailure(Call<List<Map>> call, Throwable t) {
                Log.d("myLogs", "fail: " + t.toString());
            }
        });
    }

    private String encode_s(String data) {
        byte[] base64 = Base64.encodeBase64(data.getBytes());
        String sha1 = new String(Hex.encodeHex(DigestUtils.sha1(base64)));
        return new String(Hex.encodeHex(DigestUtils.md5(sha1)));
    }

    public abstract void onStatusOk(Response<ResponseObject> response);
    public abstract void onStatusServerError(Response<ResponseObject> response);
    public abstract void onFail(Throwable t);

    // Обработка данных ответов не всегда должна быть реализована
    public void onStatus405(Response<ResponseObject> response) {};
    public void onStatus406(Response<ResponseObject> response) {};
}
