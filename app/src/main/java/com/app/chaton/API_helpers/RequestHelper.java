package com.app.chaton.API_helpers;


import android.util.Base64;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public abstract class RequestHelper {

    public static final String ACT_AUTH = "auth";

    private final static int STATUS_OK = 200;
    private final static int STATUS_SERVER_ERROR = 500;

    private final static int STATUS_405 = 405;
    private final static int STATUS_406 = 406;

    private final static Gson gson;
    static { gson = new GsonBuilder().create(); }

    public void makeResponse(CallService callService, String action, RequestObject requestObject) {
        Call<ResponseObject> call = callService.auth(
                Base64.encodeToString(action.getBytes(), Base64.DEFAULT),
                Base64.encodeToString(new GsonBuilder().create().toJson(requestObject).getBytes(), Base64.DEFAULT)
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

    public abstract void onStatusOk(Response<ResponseObject> response);
    public abstract void onStatusServerError(Response<ResponseObject> response);
    public abstract void onStatus405(Response<ResponseObject> response);
    public abstract void onStatus406(Response<ResponseObject> response);
    public abstract void onFail(Throwable t);
}
