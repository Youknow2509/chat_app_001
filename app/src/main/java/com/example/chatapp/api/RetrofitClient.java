package com.example.chatapp.api;

import android.content.Context;
import android.util.Log;

import com.example.chatapp.consts.Constants;
import com.example.chatapp.network.AuthInterceptor;
import com.example.chatapp.models.sqlite.Message;
import com.example.chatapp.network.NetworkConnectionInterceptor;
import com.example.chatapp.network.NetworkMonitor;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    private static RetrofitClient instance;
    private Retrofit retrofit;
    private Retrofit retrofitCloudinary;
    private ChatAppService chatAppService;
    private CloudinaryService cloudinaryService;
    private final NetworkMonitor networkMonitor;
    private final String TAG = "RetrofitClient";

    private RetrofitClient(Context context) {
        // Khởi tạo NetworkMonitor
        networkMonitor = NetworkMonitor.getInstance(context);

        // Setup logging interceptor
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        // Thêm NetworkConnectionInterceptor để xử lý trường hợp không có kết nối
        NetworkConnectionInterceptor networkInterceptor = new NetworkConnectionInterceptor(context);

        // Thêm AuthInterceptor để thêm token vào header
        AuthInterceptor authInterceptor = new AuthInterceptor(context);

        // Setup OkHttpClient với NetworkConnectionInterceptor
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .addInterceptor(networkInterceptor) // Thêm network interceptor đầu tiên
                .addInterceptor(authInterceptor)
                .addInterceptor(loggingInterceptor)
                .build();

        // Configure Gson
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(Date.class, new JsonDeserializer<Date>() {
                    @Override
                    public Date deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
                        try {
                            return new Date(json.getAsLong());
                        } catch (Exception e) {
                            Log.e(TAG, "Error parsing date: " + json, e);
                            return null;
                        }
                    }
                }).create();

        // Create Retrofit instance
        retrofit = new Retrofit.Builder()
                .baseUrl(Constants.URL_HOST_SERVER)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
                .build();

        retrofitCloudinary = new Retrofit.Builder()
                .baseUrl(Constants.URL_HOST_SERVER_SIGN)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
                .build();

        chatAppService = retrofit.create(ChatAppService.class);
        cloudinaryService = retrofitCloudinary.create(CloudinaryService.class);
    }

    public static synchronized RetrofitClient getInstance(Context context) {
        if (instance == null) {
            instance = new RetrofitClient(context.getApplicationContext());
        }
        return instance;
    }

    public ChatAppService getService() {
        return chatAppService;
    }

    public CloudinaryService getCloudinaryService() {
        return cloudinaryService;
    }

    public boolean isNetworkAvailable() {
        return networkMonitor.isNetworkAvailable();
    }
}