package com.example.chatapp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chatapp.R;
import com.example.chatapp.models.MentionSuggestion;

import java.util.List;

public class MentionSuggestionAdapter extends RecyclerView.Adapter<MentionSuggestionAdapter.MentionViewHolder> {
    private List<MentionSuggestion> suggestions;
    private OnMentionClickListener listener;

    public interface OnMentionClickListener {
        void onMentionClick(MentionSuggestion suggestion);
    }

    public MentionSuggestionAdapter(List<MentionSuggestion> suggestions, OnMentionClickListener listener) {
        this.suggestions = suggestions;
        this.listener = listener;
    }

    @NonNull
    @Override
    public MentionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_mention_suggestion, parent, false);
        return new MentionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MentionViewHolder holder, int position) {
        MentionSuggestion suggestion = suggestions.get(position);
        holder.mentionName.setText(suggestion.getName());
        holder.mentionIcon.setImageResource(suggestion.getIconResId());

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onMentionClick(suggestion);
            }
        });
    }

    @Override
    public int getItemCount() {
        return suggestions.size();
    }

    static class MentionViewHolder extends RecyclerView.ViewHolder {
        TextView mentionName;
        ImageView mentionIcon;

        public MentionViewHolder(@NonNull View itemView) {
            super(itemView);
            mentionName = itemView.findViewById(R.id.mentionName);
            mentionIcon = itemView.findViewById(R.id.mentionIcon);
        }
    }
}
