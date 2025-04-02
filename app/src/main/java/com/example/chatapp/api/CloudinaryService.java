package com.example.chatapp.api;

import com.example.chatapp.models.response.ResponseData;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.POST;

public interface CloudinaryService {
    // ============ Cloudinary Management ============
    @POST("/api/v1/getSignature")
    Call<ResponseData<Object>> getSignatur(@Header("Authorization") String token, @Body String configURL);
}
