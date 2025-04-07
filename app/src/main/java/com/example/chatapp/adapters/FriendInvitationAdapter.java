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

import com.example.chatapp.R;
import com.example.chatapp.models.FriendInvitation;

import java.util.List;

public class FriendInvitationAdapter extends RecyclerView.Adapter<FriendInvitationAdapter.ViewHolder> {

    private Context context;
    private List<FriendInvitation> invitations;
    private boolean isReceivedTab;

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

        holder.tvUsername.setText(invitation.getUsername());
        holder.tvEmail.setText(invitation.getEmail());
        holder.tvTime.setText(invitation.getTime());

        // Hiển thị các nút tương ứng với tab
        if (isReceivedTab) {
            holder.layoutReceivedButtons.setVisibility(View.VISIBLE);
            holder.layoutSentButtons.setVisibility(View.GONE);

            // Xử lý sự kiện cho nút chấp nhận
            holder.btnAccept.setOnClickListener(v -> {
                invitation.setStatus(FriendInvitation.STATUS_ACCEPTED);
                notifyItemChanged(position);
                // Gọi API chấp nhận lời mời
            });

            // Xử lý sự kiện cho nút từ chối
            holder.btnReject.setOnClickListener(v -> {
                invitation.setStatus(FriendInvitation.STATUS_REJECTED);
                notifyItemChanged(position);
                // Gọi API từ chối lời mời
            });
        } else {
            holder.layoutReceivedButtons.setVisibility(View.GONE);
            holder.layoutSentButtons.setVisibility(View.VISIBLE);

            // Hiển thị trạng thái lời mời đã gửi
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

            // Xử lý sự kiện cho nút hủy lời mời
            holder.btnCancel.setOnClickListener(v -> {
                invitations.remove(position);
                notifyItemRemoved(position);
                // Gọi API hủy lời mời
            });
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
}
