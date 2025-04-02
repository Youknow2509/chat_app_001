package com.example.chatapp.observers;

import com.example.chatapp.models.WebRTCMessage;

import org.webrtc.IceCandidate;
import org.webrtc.SessionDescription;

public interface SignalingObserver {
    void onOfferReceived(SessionDescription offer);
    void onAnswerReceived(SessionDescription answer);
    void onIceCandidateReceived(IceCandidate iceCandidate);
    void onSignalingEvent(WebRTCMessage message);
}
