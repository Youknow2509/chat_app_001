package com.example.chatapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.example.chatapp.R;
import com.example.chatapp.models.FriendInvitation;

import java.util.List;

public class FriendInvitationAdapter extends RecyclerView.Adapter<FriendInvitationAdapter.ViewHolder> {

    private Context context;
    private List<FriendInvitation> invitations;
    private boolean isReceivedTab;

    // Interface for handling button clicks
    private OnAcceptClickListener acceptClickListener;
    private OnRejectClickListener rejectClickListener;
    private OnCancelClickListener cancelClickListener;

    // Interface definitions
    public interface OnAcceptClickListener {
        void onAcceptClick(int position);
    }

    public interface OnRejectClickListener {
        void onRejectClick(int position);
    }

    public interface OnCancelClickListener {
        void onCancelClick(int position);
    }

    // Methods to set listeners
    public void setOnAcceptClickListener(OnAcceptClickListener listener) {
        this.acceptClickListener = listener;
    }

    public void setOnRejectClickListener(OnRejectClickListener listener) {
        this.rejectClickListener = listener;
    }

    public void setOnCancelClickListener(OnCancelClickListener listener) {
        this.cancelClickListener = listener;
    }

    public FriendInvitationAdapter(Context context, List<FriendInvitation> invitations, boolean isReceivedTab) {
        this.context = context;
        this.invitations = invitations;
        this.isReceivedTab = isReceivedTab;
    }

    public void updateData(List<FriendInvitation> invitations, boolean isReceivedTab) {
        this.invitations = invitations;
        this.isReceivedTab = isReceivedTab;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_friend_invitation, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FriendInvitation invitation = invitations.get(position);

        // Set basic user info
        holder.tvUsername.setText(invitation.getNickName());
        holder.tvEmail.setText(""); // TODO
        holder.tvTime.setText(invitation.getTime());

        // Load avatar with improved error handling
        loadAvatar(holder.imgAvatar, invitation.getAvatarUrl());

        // Reset views before setting new state
        resetViewState(holder);

        // Set up views based on invitation tab and status
        if (isReceivedTab) {
            setupReceivedInvitationView(holder, invitation, position);
        } else {
            setupSentInvitationView(holder, invitation, position);
        }
    }

    /**
     * Reset view state to default
     */
    private void resetViewState(ViewHolder holder) {
        holder.layoutReceivedButtons.setVisibility(View.GONE);
        holder.layoutSentButtons.setVisibility(View.GONE);
        holder.tvStatus.setVisibility(View.GONE);
        holder.btnAccept.setEnabled(true);
        holder.btnReject.setEnabled(true);
        holder.btnCancel.setVisibility(View.VISIBLE);
    }

    /**
     * Setup view for received invitation tab
     */
    private void setupReceivedInvitationView(ViewHolder holder, FriendInvitation invitation, int position) {
        holder.layoutReceivedButtons.setVisibility(View.VISIBLE);

        // Update UI based on invitation status
        if (invitation.getStatus() == FriendInvitation.STATUS_PENDING) {
            holder.btnAccept.setEnabled(true);
            holder.btnReject.setEnabled(true);
            holder.tvStatus.setVisibility(View.GONE);
        } else {
            holder.btnAccept.setEnabled(false);
            holder.btnReject.setEnabled(false);
            holder.tvStatus.setVisibility(View.VISIBLE);

            if (invitation.getStatus() == FriendInvitation.STATUS_ACCEPTED) {
                holder.tvStatus.setText("Đã chấp nhận");
                holder.tvStatus.setTextColor(context.getResources().getColor(R.color.colorSuccess));
            } else if (invitation.getStatus() == FriendInvitation.STATUS_REJECTED) {
                holder.tvStatus.setText("Đã từ chối");
                holder.tvStatus.setTextColor(context.getResources().getColor(R.color.colorDanger));
            }
        }

        // Set up button click listeners
        final int adapterPosition = position;
        holder.btnAccept.setOnClickListener(v -> {
            if (acceptClickListener != null && adapterPosition != RecyclerView.NO_POSITION) {
                acceptClickListener.onAcceptClick(adapterPosition);
            }
        });

        holder.btnReject.setOnClickListener(v -> {
            if (rejectClickListener != null && adapterPosition != RecyclerView.NO_POSITION) {
                rejectClickListener.onRejectClick(adapterPosition);
            }
        });
    }

    /**
     * Setup view for sent invitation tab
     */
    private void setupSentInvitationView(ViewHolder holder, FriendInvitation invitation, int position) {
        holder.layoutSentButtons.setVisibility(View.VISIBLE);
        holder.tvStatus.setVisibility(View.VISIBLE);

        // Display invitation status
        switch (invitation.getStatus()) {
            case FriendInvitation.STATUS_PENDING:
                holder.tvStatus.setText("Đang chờ");
                holder.tvStatus.setTextColor(context.getResources().getColor(R.color.colorWarning));
                holder.btnCancel.setVisibility(View.VISIBLE);
                break;
            case FriendInvitation.STATUS_ACCEPTED:
                holder.tvStatus.setText("Đã chấp nhận");
                holder.tvStatus.setTextColor(context.getResources().getColor(R.color.colorSuccess));
                holder.btnCancel.setVisibility(View.GONE);
                break;
            case FriendInvitation.STATUS_REJECTED:
                holder.tvStatus.setText("Đã từ chối");
                holder.tvStatus.setTextColor(context.getResources().getColor(R.color.colorDanger));
                holder.btnCancel.setVisibility(View.GONE);
                break;
        }

        // Set up cancel button click listener
        final int adapterPosition = position;
        holder.btnCancel.setOnClickListener(v -> {
            if (cancelClickListener != null && adapterPosition != RecyclerView.NO_POSITION) {
                cancelClickListener.onCancelClick(adapterPosition);
            }
        });
    }

    /**
     * Load avatar image with improved error handling
     */
    private void loadAvatar(ImageView imageView, String avatarUrl) {
        RequestOptions options = new RequestOptions()
                .placeholder(R.drawable.user_circle_3)
                .error(R.drawable.user_circle_3)
                .circleCrop();

        if (avatarUrl != null && !avatarUrl.isEmpty()) {
            Glide.with(context)
                    .load(avatarUrl)
                    .apply(options)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(imageView);
        } else {
            imageView.setImageResource(R.drawable.user_circle_3);
        }
    }

    @Override
    public int getItemCount() {
        return invitations.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgAvatar;
        TextView tvUsername, tvEmail, tvTime, tvStatus;
        Button btnAccept, btnReject, btnCancel;
        LinearLayout layoutReceivedButtons, layoutSentButtons;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            imgAvatar = itemView.findViewById(R.id.imgAvatar);
            tvUsername = itemView.findViewById(R.id.tvUsername);
            tvEmail = itemView.findViewById(R.id.tvEmail);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            btnAccept = itemView.findViewById(R.id.btnAccept);
            btnReject = itemView.findViewById(R.id.btnReject);
            btnCancel = itemView.findViewById(R.id.btnCancel);
            layoutReceivedButtons = itemView.findViewById(R.id.layoutReceivedButtons);
            layoutSentButtons = itemView.findViewById(R.id.layoutSentButtons);
        }
    }

    /**
     * Manually update the status of a specific invitation
     * @param position position of the invitation in the list
     * @param status new status to set
     */
    public void updateInvitationStatus(int position, int status) {
        if (position >= 0 && position < invitations.size()) {
            invitations.get(position).setStatus(status);
            notifyItemChanged(position);
        }
    }

    /**
     * Remove an invitation from the list
     * @param position position of the invitation to remove
     */
    public void removeInvitation(int position) {
        if (position >= 0 && position < invitations.size()) {
            invitations.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, invitations.size());
        }
    }
}