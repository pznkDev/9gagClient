package com.kpi.slava.a9gag_client.api;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface Link {

    @FormUrlEncoded
    @POST("/api/gag.get")
    Call<BaseResponse> getImages(@FieldMap Map<String, Integer> map);
}
