// AppPeerConnectionObserver.java
package com.example.chatapp.observers;

import android.util.Log;

import org.webrtc.DataChannel;
import org.webrtc.IceCandidate;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;

public class AppPeerConnectionObserver implements PeerConnection.Observer {
    private String tag;

    public AppPeerConnectionObserver(String tag) {
        this.tag = tag;
    }

    @Override
    public void onSignalingChange(PeerConnection.SignalingState signalingState) {
        Log.d(tag, "onSignalingChange: " + signalingState);
    }

    @Override
    public void onIceConnectionChange(PeerConnection.IceConnectionState iceConnectionState) {
        Log.d(tag, "onIceConnectionChange: " + iceConnectionState);

    }

    @Override
    public void onIceConnectionReceivingChange(boolean b) {
        Log.d(tag, "onIceConnectionReceivingChange: " + b);
    }

    @Override
    public void onIceGatheringChange(PeerConnection.IceGatheringState iceGatheringState) {
        Log.d(tag, "onIceGatheringChange: " + iceGatheringState);
    }

    @Override
    public void onIceCandidate(IceCandidate iceCandidate) {
        Log.d(tag, "onIceCandidate: " + iceCandidate);
    }

    @Override
    public void onIceCandidatesRemoved(IceCandidate[] iceCandidates) {
        Log.d(tag, "onIceCandidatesRemoved: ");
    }

    @Override
    public void onAddStream(MediaStream mediaStream) {
        Log.d(tag, "onAddStream: ");
    }

    @Override
    public void onRemoveStream(MediaStream mediaStream) {
        Log.d(tag, "onRemoveStream: ");
    }

    @Override
    public void onDataChannel(DataChannel dataChannel) {
        Log.d(tag, "onDataChannel: ");
    }

    @Override
    public void onRenegotiationNeeded() {
        Log.d(tag, "onRenegotiationNeeded: ");
    }
}