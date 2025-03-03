package com.example.chatapp.services;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

import com.example.chatapp.utilities.StompClientManager;

public class StompServiceHelper {
    private static final String TAG = "StompServiceHelper";
    private static StompServiceHelper instance;
    private StompClientManager stompService;
    private boolean isBound = false;
    private Context applicationContext;

    // Service connection object
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            StompClientManager.LocalBinder binder = (StompClientManager.LocalBinder) service;
            stompService = binder.getService();
            isBound = true;
            Log.d(TAG, "Service connected");
        }

        @Override
        public void onServiceDisconnected(ComponentName className) {
            stompService = null;
            isBound = false;
            Log.d(TAG, "Service disconnected");
        }
    };

    private StompServiceHelper() {
        // Private constructor
    }

    public static synchronized StompServiceHelper getInstance() {
        if (instance == null) {
            instance = new StompServiceHelper();
        }
        return instance;
    }

    public void initialize(Context context) {
        if (applicationContext == null) {
            applicationContext = context.getApplicationContext();
        }
        startAndBindService();
    }

    public void startAndBindService() {
        if (applicationContext != null) {
            // Start the service
            Intent intent = new Intent(applicationContext, StompClientManager.class);
            applicationContext.startService(intent);

            // Bind to the service
            applicationContext.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
        }
    }

    public void unbindService() {
        if (isBound && applicationContext != null) {
            applicationContext.unbindService(serviceConnection);
            isBound = false;
        }
    }

    public StompClientManager getService() {
        return stompService;
    }

    public boolean isServiceBound() {
        return isBound;
    }
}