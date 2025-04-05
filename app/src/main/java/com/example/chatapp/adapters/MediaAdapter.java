package com.example.chatapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.chatapp.R;
import com.example.chatapp.models.Media;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class MediaAdapter extends RecyclerView.Adapter<MediaAdapter.MediaViewHolder> {

    private List<Media> mediaList;
    private OnMediaItemClickListener listener;
    private boolean multiSelectMode = false;
    private Set<Integer> selectedItems = new HashSet<>();
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    public interface OnMediaItemClickListener {
        void onMediaItemClick(Media media, int position);
        void onDeleteClick(Media media, int position);
        void onSelectionChanged(int count);
    }

    public MediaAdapter(List<Media> mediaList, OnMediaItemClickListener listener) {
        this.mediaList = mediaList != null ? mediaList : new ArrayList<>();
        this.listener = listener;
    }

    public void updateData(List<Media> newMediaList) {
        this.mediaList = newMediaList != null ? newMediaList : new ArrayList<>();
        notifyDataSetChanged();
    }

    public void toggleSelectionMode() {
        multiSelectMode = !multiSelectMode;
        if (!multiSelectMode) {
            selectedItems.clear();
            if (listener != null) {
                listener.onSelectionChanged(0);
            }
        }
        notifyDataSetChanged();
    }

    public boolean isInMultiSelectMode() {
        return multiSelectMode;
    }

    public void toggleSelection(int position) {
        if (selectedItems.contains(position)) {
            selectedItems.remove(position);
        } else {
            selectedItems.add(position);
        }
        notifyItemChanged(position);

        if (listener != null) {
            listener.onSelectionChanged(selectedItems.size());
        }
    }

    public List<Media> getSelectedItems() {
        List<Media> selected = new ArrayList<>();
        for (Integer position : selectedItems) {
            if (position < mediaList.size()) {
                selected.add(mediaList.get(position));
            }
        }
        return selected;
    }

    public List<Integer> getSelectedPositions() {
        return new ArrayList<>(selectedItems);
    }

    public void clearSelection() {
        selectedItems.clear();
        notifyDataSetChanged();
        if (listener != null) {
            listener.onSelectionChanged(0);
        }
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
        holder.bind(media, position, multiSelectMode, selectedItems.contains(position), listener);
    }

    @Override
    public int getItemCount() {
        return mediaList.size();
    }

    public List<Media> getItems() {
        return mediaList;
    }

    static class MediaViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivMediaThumbnail;
        private ImageView ivMediaType;
        private TextView tvFileName;
        private TextView tvFileDate;
        private TextView tvFileSize;
        private ImageButton btnDelete;
        private CheckBox checkboxSelect;

        public MediaViewHolder(@NonNull View itemView) {
            super(itemView);
            ivMediaThumbnail = itemView.findViewById(R.id.ivMediaThumbnail);
            ivMediaType = itemView.findViewById(R.id.ivMediaType);
            tvFileName = itemView.findViewById(R.id.tvFileName);
            tvFileDate = itemView.findViewById(R.id.tvFileDate);
            tvFileSize = itemView.findViewById(R.id.tvFileSize);
            btnDelete = itemView.findViewById(R.id.btnDelete);
            checkboxSelect = itemView.findViewById(R.id.checkboxSelect);
        }

        public void bind(final Media media, final int position, boolean multiSelectMode,
                         boolean isSelected, final OnMediaItemClickListener listener) {
            tvFileName.setText(media.getNameFile());
            tvFileSize.setText(formatSize(media.getKb()));

            // Set file date
            File file = new File(media.getPathFile());
            if (file.exists()) {
                long lastModified = file.lastModified();
                Date date = new Date(lastModified);
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                tvFileDate.setText(sdf.format(date));
            } else {
                tvFileDate.setText("Bot-SomeOne | 2025-04-05"); // Current user and date
            }

            // Load thumbnail based on media type
            Context context = itemView.getContext();
            String type = media.getType();

            // Show appropriate icon based on media type
            int typeIconRes;
            switch (type) {
                case "image":
                    typeIconRes = R.drawable.ic_image;
                    loadImageThumbnail(context, media.getPathFile());
                    break;
                case "video":
                    typeIconRes = R.drawable.ic_video;
                    loadVideoThumbnail(context, media.getPathFile());
                    break;
                case "thumbnail":
                    typeIconRes = R.drawable.ic_thumbnail;
                    loadImageThumbnail(context, media.getPathFile());
                    break;
                case "record":
                case "audio":
                    typeIconRes = R.drawable.ic_audio;
                    loadAudioThumbnail(context);
                    break;
                default:
                    typeIconRes = R.drawable.ic_file;
                    loadDefaultThumbnail(context);
                    break;
            }
            ivMediaType.setImageResource(typeIconRes);

            // Handle multi-select mode
            if (multiSelectMode) {
                checkboxSelect.setVisibility(View.VISIBLE);
                checkboxSelect.setChecked(isSelected);
                btnDelete.setVisibility(View.GONE);
            } else {
                checkboxSelect.setVisibility(View.GONE);
                btnDelete.setVisibility(View.VISIBLE);
            }

            // Set click listeners
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    if (multiSelectMode) {
                        checkboxSelect.setChecked(!checkboxSelect.isChecked());
                        listener.onMediaItemClick(media, position);
                    } else {
                        listener.onMediaItemClick(media, position);
                    }
                }
            });

            btnDelete.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDeleteClick(media, position);
                }
            });

            checkboxSelect.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onMediaItemClick(media, position);
                }
            });
        }

        private void loadImageThumbnail(Context context, String path) {
            Glide.with(context)
                    .load(new File(path))
                    .apply(new RequestOptions()
                            .centerCrop()
                            .placeholder(R.drawable.ic_image))
                    .into(ivMediaThumbnail);
        }

        private void loadVideoThumbnail(Context context, String path) {
            Glide.with(context)
                    .asBitmap()
                    .load(new File(path))
                    .apply(new RequestOptions()
                            .centerCrop()
                            .placeholder(R.drawable.ic_video))
                    .into(ivMediaThumbnail);
        }

        private void loadAudioThumbnail(Context context) {
            // Load a placeholder for audio
            ivMediaThumbnail.setImageResource(R.drawable.audio_placeholder);
            ivMediaThumbnail.setScaleType(ImageView.ScaleType.CENTER);
        }

        private void loadDefaultThumbnail(Context context) {
            // Load a default placeholder
            ivMediaThumbnail.setImageResource(R.drawable.file_placeholder);
            ivMediaThumbnail.setScaleType(ImageView.ScaleType.CENTER);
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