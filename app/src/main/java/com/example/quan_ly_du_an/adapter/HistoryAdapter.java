package com.example.quan_ly_du_an.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.quan_ly_du_an.R;
import com.example.quan_ly_du_an.model.History;
import java.util.ArrayList;
import java.util.List;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder> {
    private List<History> historyList = new ArrayList<>();
    private List<History> historyListFull = new ArrayList<>();

    public void setHistoryList(List<History> list) {
        this.historyList = new ArrayList<>(list);
        this.historyListFull = new ArrayList<>(list);
        notifyDataSetChanged();
    }

    public void filter(String text) {
        historyList.clear();
        if (text.isEmpty()) {
            historyList.addAll(historyListFull);
        } else {
            String query = text.toLowerCase().trim();
            for (History item : historyListFull) {
                if (item.getTitle().toLowerCase().contains(query) || 
                    item.getDescription().toLowerCase().contains(query)) {
                    historyList.add(item);
                }
            }
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_history, parent, false);
        return new HistoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryViewHolder holder, int position) {
        History history = historyList.get(position);
        holder.tvTitle.setText(history.getTitle());
        holder.tvDesc.setText(history.getDescription());
        holder.tvTime.setText(history.getTimestamp());
    }

    @Override
    public int getItemCount() {
        return historyList.size();
    }

    static class HistoryViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvDesc, tvTime;
        HistoryViewHolder(View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvHistoryTitle);
            tvDesc = itemView.findViewById(R.id.tvHistoryDesc);
            tvTime = itemView.findViewById(R.id.tvHistoryTime);
        }
    }
}
