package com.example.chatapp.observers;

import android.util.Log;

import org.webrtc.SdpObserver;
import org.webrtc.SessionDescription;

public class SdpObservable implements SdpObserver {
    private static final String TAG = "SdpObservable";
    @Override
    public void onCreateSuccess(SessionDescription sessionDescription) {
        Log.i(TAG, "onCreateSuccess: ");
    }

    @Override
    public void onSetSuccess() {
        Log.i(TAG, "onSetSuccess: ");
    }

    @Override
    public void onCreateFailure(String s) {
        Log.e(TAG, "ERR: onCreateFailure: "+ s);
    }

    @Override
    public void onSetFailure(String s) {
        Log.e(TAG, "ERR: onSetFailure: "+ s);
    }
}
