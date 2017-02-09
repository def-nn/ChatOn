package com.app.chaton.API_helpers;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface CallService {

    String ACT = "act";
    String ARGS = "args";

    @FormUrlEncoded
    @POST("./")
    Call<ResponseObject> auth(@Field(ACT) String act, @Field(ARGS) String args);

}
