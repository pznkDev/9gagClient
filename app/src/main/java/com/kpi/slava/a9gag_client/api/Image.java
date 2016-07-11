package com.kpi.slava.a9gag_client.api;

import com.google.gson.annotations.SerializedName;

public class Image {

    @SerializedName("id")
    public String id;
    @SerializedName("image_mini")
    public String image_mini;
    @SerializedName("image_full")
    public String image_full;


    public Image(String image_full, String id, String image_mini) {
        this.image_full = image_full;
        this.id = id;
        this.image_mini = image_mini;
    }

    public Image() {
    }

    public String getId() {
        return id;
    }

    public String getImage_mini() {
        return image_mini;
    }

    public String getImage_full() {
        return image_full;
    }
}
