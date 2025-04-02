package com.example.chatapp.api;

import com.example.chatapp.consts.Constants;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    private static RetrofitClient instance;
    private Retrofit retrofit;
    private ChatAppService chatAppService;
    private CloudinaryService cloudinaryService;

    private RetrofitClient() {
        // Setup logging interceptor
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        // Setup OkHttpClient
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .addInterceptor(loggingInterceptor)
                .build();

        // Configure Gson
        Gson gson = new GsonBuilder()
                .setLenient()
                .create();

        // Create Retrofit instance
        retrofit = new Retrofit.Builder()
                .baseUrl(Constants.URL_HOST_SERVER)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
                .build();

        chatAppService = retrofit.create(ChatAppService.class);
        cloudinaryService = retrofit.create(CloudinaryService.class);
    }

    public static synchronized RetrofitClient getInstance() {
        if (instance == null) {
            instance = new RetrofitClient();
        }
        return instance;
    }

    public ChatAppService getService() {
        return chatAppService;
    }

    public CloudinaryService getCloudinaryService() {
        return cloudinaryService;
    }
}