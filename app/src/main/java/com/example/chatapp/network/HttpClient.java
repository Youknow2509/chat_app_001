package com.example.chatapp.network;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import okhttp3.*;

import com.example.chatapp.consts.Constants;
import com.example.chatapp.utils.EndPoint;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class HttpClient {

    // Reusable OkHttpClient for all requests
    private static final OkHttpClient client = new OkHttpClient();

    // MediaType JSON definition moved outside the method for efficiency
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    // PURPOSE REGISTER TEST
    private final String PURPOSE_REGISTER_TEST = "TEST_USER";
    private final String PURPOSE_REGISTER_PROD = "ANDROID_APP";


    // Login to the server using the provided email and password
    public CompletableFuture<JsonObject> login(String email, String password) {
        CompletableFuture<JsonObject> future = new CompletableFuture<>();

        String url = Constants.URL_HOST_SERVER + EndPoint.LOGIN;

        // Create JSON body for login request
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("user_account", email);
        jsonObject.addProperty("user_password", password);
        RequestBody body = RequestBody.create(jsonObject.toString(), JSON);

        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();

        // Asynchronous network call
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                // Complete future exceptionally in case of failure
                future.completeExceptionally(new IOException("Network request failed", e));
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String responseBody = response.body().string();

                // Parse the response body as JSON
                JsonObject jsonResponse = JsonParser.parseString(responseBody).getAsJsonObject();
                future.complete(jsonResponse);
            }
        });

        return future;
    }

    // register user to the server
    public CompletableFuture<JsonObject> register(String email) {
        CompletableFuture<JsonObject> future = new CompletableFuture<>();

        String url = Constants.URL_HOST_SERVER + EndPoint.REGISTER;

        // Create JSON body for login request
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("verify_key", email);
        jsonObject.addProperty("verify_purpose", PURPOSE_REGISTER_PROD); // use PURPOSE_REGISTER_TEST -> otp test 123456
        jsonObject.addProperty("verify_type", 1);

        RequestBody body = RequestBody.create(jsonObject.toString(), JSON);

        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();

        // Asynchronous network call
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                // Complete future exceptionally in case of failure
                future.completeExceptionally(new IOException("Network request failed", e));
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String responseBody = response.body().string();

                // Parse the response body as JSON
                JsonObject jsonResponse = JsonParser.parseString(responseBody).getAsJsonObject();
                future.complete(jsonResponse);
            }
        });

        return future;
    }

    // verify otp
    public CompletableFuture<JsonObject> verifyOtp(String email, String otp) {
        CompletableFuture<JsonObject> future = new CompletableFuture<>();

        String url = Constants.URL_HOST_SERVER + EndPoint.VERIFY_ACCOUNT;

        // Create JSON body for login request
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("verify_key", email);
        jsonObject.addProperty("verify_code", otp);

        RequestBody body = RequestBody.create(jsonObject.toString(), JSON);

        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();

        // Asynchronous network call
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                // Complete future exceptionally in case of failure
                future.completeExceptionally(new IOException("Network request failed", e));
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String responseBody = response.body().string();

                // Parse the response body as JSON
                JsonObject jsonResponse = JsonParser.parseString(responseBody).getAsJsonObject();
                future.complete(jsonResponse);
            }
        });

        return future;
    }


    // create password when register
    public CompletableFuture<JsonObject> createPassword(String token, String password) {
        // TODO
        return null;
    }
}
