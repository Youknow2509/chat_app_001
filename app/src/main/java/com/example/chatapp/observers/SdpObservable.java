package com.example.chatapp.observers;

import org.webrtc.SdpObserver;
import org.webrtc.SessionDescription;

public class SdpObservable implements SdpObserver {
    @Override
    public void onCreateSuccess(SessionDescription sessionDescription) {
        System.out.println("onCreateSuccess: " + sessionDescription.description);
    }

    @Override
    public void onSetSuccess() {
        System.out.println("onSetSuccess");
    }

    @Override
    public void onCreateFailure(String s) {
        System.out.println("onCreateFailure: " + s);
    }

    @Override
    public void onSetFailure(String s) {
        System.out.println("onSetFailure: " + s);
    }
}
