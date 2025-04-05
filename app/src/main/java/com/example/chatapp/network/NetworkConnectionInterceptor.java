package com.example.chatapp.network;

import android.content.Context;

import com.example.chatapp.R;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class NetworkConnectionInterceptor implements Interceptor {
    private final Context context;
    private final NetworkMonitor networkMonitor;

    public NetworkConnectionInterceptor(Context context) {
        this.context = context;
        this.networkMonitor = NetworkMonitor.getInstance(context);
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        if (!networkMonitor.isNetworkAvailable()) {
            throw new NoConnectivityException(context.getString(R.string.no_internet_connection));
        }

        Request.Builder builder = chain.request().newBuilder();
        return chain.proceed(builder.build());
    }

    public static class NoConnectivityException extends IOException {
        public NoConnectivityException(String message) {
            super(message);
        }
    }
}