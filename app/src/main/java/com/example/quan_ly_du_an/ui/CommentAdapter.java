package com.example.quan_ly_du_an.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.quan_ly_du_an.R;
import com.example.quan_ly_du_an.database.Comment;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentViewHolder> {
    private List<Comment> commentList = new ArrayList<>();
    private java.util.Map<Integer, String> userNamesMap = new java.util.HashMap<>();

    public void setUserMap(java.util.Map<Integer, String> map) {
        if (map != null) {
            this.userNamesMap = map;
            notifyDataSetChanged();
        }
    }

    public void submitList(List<Comment> comments) {
        this.commentList = comments != null ? comments : new ArrayList<>();
        notifyDataSetChanged();
    }
    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_comment, parent, false);
        return new CommentViewHolder(view);
    }
    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
        Comment currentComment = commentList.get(position);

        String userName = userNamesMap.get(currentComment.userId);
        if (userName == null || userName.isEmpty()) {
            if (currentComment.userId == 999) {
                userName = "Quản trị viên hệ thống";
            } else {
                userName = "Thành viên #" + currentComment.userId;
            }
        }
        holder.tvUserName.setText(userName);
        holder.tvContent.setText(currentComment.content);

        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm dd/MM/yyyy", Locale.getDefault());
        String dateString = sdf.format(new Date(currentComment.timestamp));
        holder.tvTime.setText(dateString);
    }
    @Override
    public int getItemCount() {
        return commentList.size();
    }
    public static class CommentViewHolder extends RecyclerView.ViewHolder {
        TextView tvUserName, tvContent, tvTime;
        public CommentViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUserName = itemView.findViewById(R.id.tvUserName);
            tvContent = itemView.findViewById(R.id.tvContent);
            tvTime = itemView.findViewById(R.id.tvTime);
        }
    }
}
