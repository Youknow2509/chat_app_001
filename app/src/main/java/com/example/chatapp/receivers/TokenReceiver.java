package com.example.chatapp.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.chatapp.activities.LoginActivity;

/**
 * BroadcastReceiver để nhận thông báo khi token được refresh
 */
public class TokenReceiver extends BroadcastReceiver {
    private static final String TAG = "TokenReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if (action != null) {
            switch (action) {
                case "com.example.chatapp.TOKEN_REFRESHED":
                    Log.d(TAG, "Token successfully refreshed");
                    break;

                case "com.example.chatapp.SESSION_EXPIRED":
                    Log.d(TAG, "Session expired, redirecting to login");

                    Intent loginIntent = new Intent(context, LoginActivity.class);
                    loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    loginIntent.putExtra("session_expired", true);
                    context.startActivity(loginIntent);
                    break;
            }
        }
    }
}