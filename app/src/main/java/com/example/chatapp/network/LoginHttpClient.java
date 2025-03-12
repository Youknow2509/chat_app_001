package com.example.chatapp.network;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import okhttp3.*;

import com.example.chatapp.consts.Constants;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class LoginHttpClient {

    // Reusable OkHttpClient for all requests
    private static final OkHttpClient client = new OkHttpClient();

    // MediaType JSON definition moved outside the method for efficiency
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    // Login to the server using the provided email and password
    public CompletableFuture<JsonObject> login(String email, String password) {
        CompletableFuture<JsonObject> future = new CompletableFuture<>();

        String url = Constants.URL_HOST_SERVER + "/v1/user/login";

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
}
