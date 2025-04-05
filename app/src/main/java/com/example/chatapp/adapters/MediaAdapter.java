package com.example.chatapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chatapp.R;
import com.example.chatapp.models.Media;

import java.util.List;

public class MediaAdapter extends RecyclerView.Adapter<MediaAdapter.MediaViewHolder> {

    private List<Media> mediaList;
    private OnMediaItemClickListener listener;

    public List<Media> getItems() {
        return mediaList;
    }

    public interface OnMediaItemClickListener {
        void onMediaItemClick(Media media);
        void onDeleteClick(Media media, int position);
    }

    public MediaAdapter(List<Media> mediaList, OnMediaItemClickListener listener) {
        this.mediaList = mediaList;
        this.listener = listener;
    }

    public void updateData(List<Media> newMediaList) {
        this.mediaList = newMediaList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MediaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_media, parent, false);
        return new MediaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MediaViewHolder holder, int position) {
        Media media = mediaList.get(position);
        holder.bind(media, listener);
    }

    @Override
    public int getItemCount() {
        return mediaList != null ? mediaList.size() : 0;
    }

    static class MediaViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivMediaType;
        private TextView tvFileName;
        private TextView tvFilePath;
        private TextView tvFileSize;
        private ImageButton btnDelete;

        public MediaViewHolder(@NonNull View itemView) {
            super(itemView);
            ivMediaType = itemView.findViewById(R.id.ivMediaType);
            tvFileName = itemView.findViewById(R.id.tvFileName);
            tvFilePath = itemView.findViewById(R.id.tvFilePath);
            tvFileSize = itemView.findViewById(R.id.tvFileSize);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }

        public void bind(final Media media, final OnMediaItemClickListener listener) {
            tvFileName.setText(media.getNameFile());
            tvFilePath.setText(media.getPathFile());
            tvFileSize.setText(formatSize(media.getKb()));

            // Set appropriate icon based on media type
            Context context = itemView.getContext();
            int iconResId;

            switch (media.getType()) {
                case "image":
                    iconResId = R.drawable.ic_image;
                    break;
                case "video":
                    iconResId = R.drawable.ic_video;
                    break;
                case "thumbnail":
                    iconResId = R.drawable.ic_thumbnail;
                    break;
                case "record":
                case "audio":
                    iconResId = R.drawable.ic_audio;
                    break;
                default:
                    iconResId = R.drawable.ic_file;
                    break;
            }

            ivMediaType.setImageResource(iconResId);

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onMediaItemClick(media);
                }
            });

            btnDelete.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDeleteClick(media, getAdapterPosition());
                }
            });
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
}