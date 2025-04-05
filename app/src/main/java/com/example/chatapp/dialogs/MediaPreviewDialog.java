package com.example.chatapp.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.VideoView;

import com.bumptech.glide.Glide;
import com.example.chatapp.R;
import com.example.chatapp.models.Media;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MediaPreviewDialog extends Dialog {

    private Media media;
    private ImageView ivPreview;
    private VideoView videoPreview;
    private TextView tvFileName;
    private ImageButton btnClose;

    public MediaPreviewDialog(Context context, Media media) {
        super(context, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        this.media = media;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_media_preview);

        getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT);

        ivPreview = findViewById(R.id.ivPreview);
        videoPreview = findViewById(R.id.videoPreview);
        tvFileName = findViewById(R.id.tvFileName);
        btnClose = findViewById(R.id.btnClose);

        // Add current date and username to the displayed filename
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String dateInfo = "Bot-SomeOne | 2025-04-05";
        tvFileName.setText(media.getNameFile() + " (" + formatSize(media.getKb()) + ") - " + dateInfo);

        btnClose.setOnClickListener(v -> dismiss());

        setupPreview();
    }

    private void setupPreview() {
        String type = media.getType();
        File mediaFile = new File(media.getPathFile());

        if (!mediaFile.exists()) {
            // Show error if file doesn't exist
            ivPreview.setImageResource(R.drawable.ic_error);
            ivPreview.setVisibility(View.VISIBLE);
            videoPreview.setVisibility(View.GONE);
            return;
        }

        switch (type) {
            case "image":
            case "thumbnail":
                // Load image
                ivPreview.setVisibility(View.VISIBLE);
                videoPreview.setVisibility(View.GONE);

                Glide.with(getContext())
                        .load(mediaFile)
                        .into(ivPreview);
                break;

            case "video":
                // Setup video
                ivPreview.setVisibility(View.GONE);
                videoPreview.setVisibility(View.VISIBLE);

                videoPreview.setVideoURI(Uri.fromFile(mediaFile));
                videoPreview.setOnPreparedListener(mp -> {
                    mp.setLooping(true);
                    videoPreview.start();
                });
                videoPreview.setOnErrorListener((mp, what, extra) -> {
                    // Show error image in case of video error
                    ivPreview.setImageResource(R.drawable.ic_error);
                    ivPreview.setVisibility(View.VISIBLE);
                    videoPreview.setVisibility(View.GONE);
                    return true;
                });
                break;

            case "audio":
            case "record":
                // Show audio waveform placeholder
                ivPreview.setVisibility(View.VISIBLE);
                videoPreview.setVisibility(View.GONE);
                ivPreview.setImageResource(R.drawable.audio_waveform);

                // Setup audio player if needed
                // Could add MediaPlayer here for audio playback
                break;

            default:
                // Show file icon for unknown types
                ivPreview.setVisibility(View.VISIBLE);
                videoPreview.setVisibility(View.GONE);
                ivPreview.setImageResource(R.drawable.file_placeholder);
                break;
        }
    }

    @Override
    public void dismiss() {
        if (videoPreview.isPlaying()) {
            videoPreview.stopPlayback();
        }
        super.dismiss();
    }

    private String formatSize(long kb) {
        if (kb < 1024) {
            return kb + " KB";
        } else {
            float mb = kb / 1024f;
            return String.format("%.2f MB", mb);
        }
    }
}