package com.example.chatapp.network;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.POST;

public interface ApiService {

    @POST("messages:send")
    Call<String> sendMessage(
            @Header("Authorization") String authToken,  // Sử dụng Bearer Token
            @Body String messageBody
    );
}
