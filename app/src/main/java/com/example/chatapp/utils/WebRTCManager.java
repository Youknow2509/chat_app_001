package com.example.chatapp.utils;

import android.content.Context;
import android.util.Log;

import org.webrtc.AudioSource;
import org.webrtc.AudioTrack;
import org.webrtc.Camera1Enumerator;
import org.webrtc.Camera2Enumerator;
import org.webrtc.CameraEnumerator;
import org.webrtc.CameraVideoCapturer;
import org.webrtc.DefaultVideoDecoderFactory;
import org.webrtc.DefaultVideoEncoderFactory;
import org.webrtc.EglBase;
import org.webrtc.IceCandidate;
import org.webrtc.MediaConstraints;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.SdpObserver;
import org.webrtc.SessionDescription;
import org.webrtc.SurfaceTextureHelper;
import org.webrtc.SurfaceViewRenderer;
import org.webrtc.VideoDecoderFactory;
import org.webrtc.VideoEncoderFactory;
import org.webrtc.VideoSource;
import org.webrtc.VideoTrack;

import java.util.ArrayList;
import java.util.List;

public class WebRTCManager {
    private static final String TAG = "WebRTCManager";

    private PeerConnectionFactory peerConnectionFactory;
    private PeerConnection peerConnection;
    private EglBase eglBase;
    private MediaStream mediaStream;
    private VideoTrack localVideoTrack;
    private AudioTrack localAudioTrack;
    private CameraVideoCapturer videoCapturer;
    private boolean isVideoEnabled;
    private boolean isAudioEnabled;

    private static WebRTCManager instance;

    public static synchronized WebRTCManager getInstance() {
        if (instance == null) {
            instance = new WebRTCManager();
        }
        return instance;
    }

    private WebRTCManager() {
        eglBase = EglBase.create();
    }

    public void init(Context context, boolean useCamera2) {
        // Initialize PeerConnectionFactory
        PeerConnectionFactory.InitializationOptions initOptions = PeerConnectionFactory.InitializationOptions.builder(context)
                .setEnableInternalTracer(true)
                .createInitializationOptions();
        PeerConnectionFactory.initialize(initOptions);

        // Create encoder/decoder factories
        VideoEncoderFactory encoderFactory = new DefaultVideoEncoderFactory(
                eglBase.getEglBaseContext(), true, true);
        VideoDecoderFactory decoderFactory = new DefaultVideoDecoderFactory(
                eglBase.getEglBaseContext());

        // Create the factory
        PeerConnectionFactory.Options options = new PeerConnectionFactory.Options();
        peerConnectionFactory = PeerConnectionFactory.builder()
                .setOptions(options)
                .setVideoEncoderFactory(encoderFactory)
                .setVideoDecoderFactory(decoderFactory)
                .createPeerConnectionFactory();

        // Create video capturer
        videoCapturer = createVideoCapturer(context, useCamera2);
    }

    public EglBase getEglBase() {
        return eglBase;
    }

    private CameraVideoCapturer createVideoCapturer(Context context, boolean useCamera2) {
        CameraEnumerator enumerator;
        if (useCamera2) {
            enumerator = new Camera2Enumerator(context);
        } else {
            enumerator = new Camera1Enumerator(false);
        }

        String[] deviceNames = enumerator.getDeviceNames();

        // Try to find front facing camera first
        for (String deviceName : deviceNames) {
            if (enumerator.isFrontFacing(deviceName)) {
                CameraVideoCapturer capturer = enumerator.createCapturer(deviceName, null);
                if (capturer != null) {
                    return capturer;
                }
            }
        }

        // If no front camera, use back camera
        for (String deviceName : deviceNames) {
            if (!enumerator.isFrontFacing(deviceName)) {
                CameraVideoCapturer capturer = enumerator.createCapturer(deviceName, null);
                if (capturer != null) {
                    return capturer;
                }
            }
        }

        return null;
    }

    public void startLocalMediaStream(Context context, SurfaceViewRenderer localVideoView, boolean isAudioEnabled, boolean isVideoEnabled) {
        this.isAudioEnabled = isAudioEnabled;
        this.isVideoEnabled = isVideoEnabled;
        mediaStream = peerConnectionFactory.createLocalMediaStream("ARDAMS");
        if (isAudioEnabled) {
            // Create audio source
            MediaConstraints audioConstraints = new MediaConstraints();
            AudioSource audioSource = peerConnectionFactory.createAudioSource(audioConstraints);
            localAudioTrack = peerConnectionFactory.createAudioTrack("ARDAMSa0", audioSource);
            localAudioTrack.setEnabled(isAudioEnabled);
            mediaStream.addTrack(localAudioTrack);
        }

        // Create video source and track
        if (isVideoEnabled) {
            // Configure local video view
            localVideoView.init(eglBase.getEglBaseContext(), null);
            localVideoView.setEnableHardwareScaler(true);
            localVideoView.setMirror(true);
            SurfaceTextureHelper surfaceTextureHelper = SurfaceTextureHelper.create("CaptureThread", eglBase.getEglBaseContext());
            VideoSource videoSource = peerConnectionFactory.createVideoSource(videoCapturer.isScreencast());
            videoCapturer.initialize(surfaceTextureHelper, context, videoSource.getCapturerObserver());
            videoCapturer.startCapture(1280, 720, 30);  // Width, height, fps

            localVideoTrack = peerConnectionFactory.createVideoTrack("ARDAMSv0", videoSource);
            localVideoTrack.setEnabled(isVideoEnabled);
            localVideoTrack.addSink(localVideoView);

            mediaStream.addTrack(localVideoTrack);
        }

        // Create media stream


    }

    public PeerConnection createPeerConnection(PeerConnection.Observer observer) {
        List<PeerConnection.IceServer> iceServers = new ArrayList<>();
        iceServers.add(PeerConnection.IceServer.builder("stun:stun.l.google.com:19302").createIceServer());

        iceServers.add(PeerConnection.IceServer.builder("turn:khanhdew.ddns.net:3478")
                .setUsername("a")
                .setPassword("a")
                .createIceServer());

        PeerConnection.RTCConfiguration rtcConfig = new PeerConnection.RTCConfiguration(iceServers);
        rtcConfig.sdpSemantics = PeerConnection.SdpSemantics.UNIFIED_PLAN;

        peerConnection = peerConnectionFactory.createPeerConnection(rtcConfig, observer);

        if (peerConnection != null) {
            // Create audio source and track if it's not initialized
            if (localAudioTrack == null) {
                MediaConstraints audioConstraints = new MediaConstraints();
                AudioSource audioSource = peerConnectionFactory.createAudioSource(audioConstraints);
                localAudioTrack = peerConnectionFactory.createAudioTrack("ARDAMSa0", audioSource);
                localAudioTrack.setEnabled(isAudioEnabled);
            }

            if (localVideoTrack != null) {
                peerConnection.addTrack(localVideoTrack);
            }
            peerConnection.addTrack(localAudioTrack);
        }

        return peerConnection;
    }

    public void createOffer(MediaConstraints constraints, SdpObserver observer) {
        Log.i(TAG, "createOffer: " + constraints);
        if (peerConnection != null) {
            Log.i(TAG, "createOffer: " + peerConnection);
            peerConnection.createOffer(observer, constraints);
        }
    }

    public void createAnswer(MediaConstraints constraints, SdpObserver observer) {
        if (peerConnection != null) {
            peerConnection.createAnswer(observer, constraints);
        }
    }

    public void setLocalDescription(SdpObserver observer, SessionDescription sdp) {
        if (peerConnection != null) {
            peerConnection.setLocalDescription(observer, sdp);
        }
    }

    public void setRemoteDescription(SdpObserver observer, SessionDescription sdp) {
        if (peerConnection != null) {
            peerConnection.setRemoteDescription(observer, sdp);
        }
    }

    public void addIceCandidate(IceCandidate iceCandidate) {
        if (peerConnection != null) {
            peerConnection.addIceCandidate(iceCandidate);
        }
    }

    public void toggleVideo(boolean isEnabled) {
        isVideoEnabled = isEnabled;
        if (localVideoTrack != null) {
            localVideoTrack.setEnabled(isEnabled);
        }
    }

    public void toggleAudio(boolean isEnabled) {
        isAudioEnabled = isEnabled;
        if (localAudioTrack != null) {
            localAudioTrack.setEnabled(isEnabled);
        }
    }

    public void switchCamera() {
        if (videoCapturer != null) {
            ((CameraVideoCapturer) videoCapturer).switchCamera(null);
        }
    }

    public void release() {
        if (videoCapturer != null) {
            try {
                videoCapturer.stopCapture();
            } catch (InterruptedException e) {
                Log.e(TAG, "Failed to stop camera", e);
            }
            videoCapturer.dispose();
            videoCapturer = null;
        }

        if (peerConnection != null) {
            peerConnection.close();
            peerConnection = null;
        }

        if (peerConnectionFactory != null) {
            peerConnectionFactory.dispose();
            peerConnectionFactory = null;
        }

        if (eglBase != null) {
            eglBase.release();
            eglBase = null;
        }

        instance = null;
    }

    public SessionDescription getLocalDescription() {
        if (peerConnection != null) {
            return peerConnection.getLocalDescription();
        }
        return null;
    }
}