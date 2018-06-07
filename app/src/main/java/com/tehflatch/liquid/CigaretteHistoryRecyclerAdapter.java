package com.tehflatch.aquafy;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import java.util.List;

public class CigaretteHistoryRecyclerAdapter extends RecyclerView.Adapter {
    private List<CigaretteHistoryModel> cigaretteList;
    private int mExpandedPosition = -1;

    public CigaretteHistoryRecyclerAdapter(List<CigaretteHistoryModel> cigaretteList) {
        this.cigaretteList = cigaretteList;
    }
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View view = LayoutInflater.from(parent.getContext()).inflate(viewType, parent, false);
        return new CigaretteHistoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
        ((CigaretteHistoryViewHolder) holder).bindData(cigaretteList.get(position));
        final View v = holder.itemView;
        final LinearLayout buttonHolder = v.findViewById(R.id.buttonHolder);
        v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean expanded = (buttonHolder.getVisibility() == View.VISIBLE);
                if (!expanded) {
                    buttonHolder.setVisibility(View.VISIBLE);
                } else {
                    buttonHolder.setVisibility(View.GONE);
                    notifyItemChanged(holder.getAdapterPosition());
                }

                //notifyDataSetChanged();
            }
        });
    }

    @Override
    public int getItemViewType(int position) {
        return R.layout.cigarette_history_card;
    }

    @Override
    public int getItemCount() {
        return cigaretteList.size();
    }
}