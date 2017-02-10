package com.app.chaton.API_helpers;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Header;
import retrofit2.http.POST;

public interface CallService {

    String ACT = "act";
    String ARGS = "args";

    String HEADER_U = "AUTH_U";
    String HEADER_S = "AUTH_S";

    @FormUrlEncoded
    @POST("./")
    Call<ResponseObject> auth(@Field(ACT) String act, @Field(ARGS) String args);

    @FormUrlEncoded
    @POST("./")
    Call<List<Map>> getDialogs(@Header(HEADER_U) Long _u, @Header(HEADER_S) String secret_key,
                               @Field(ACT) String act, @Field(ARGS) String args);

}
