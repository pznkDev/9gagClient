package com.kpi.slava.a9gag_client.api;


import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class BaseResponse {

    @SerializedName("success")
    public boolean success;

    @SerializedName("data")
    public List<Image> list = new ArrayList<>();
}
