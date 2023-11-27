package com.zhihu.matisse.ui.labels;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.engineer.ai.model.Labels;
import com.zhihu.matisse.R;

import java.util.ArrayList;
import java.util.List;

public class LabelCategoryAdapter extends RecyclerView.Adapter<LabelCategoryAdapter.MyHolder> {

    private final List<Labels> labelsList = new ArrayList<>();

    private int imageSize = 0;

    @SuppressLint("NotifyDataSetChanged")
    public void update(List<Labels> labelsList) {
        this.labelsList.clear();
        this.labelsList.addAll(labelsList);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        imageSize = parent.getContext().getResources().getDisplayMetrics().widthPixels / 3;
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.label_grid_content, parent, false);
        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {
        Labels labels = labelsList.get(position);
        holder.labelTv.setText(labels.getLabel());
        holder.labelNum.setText(String.valueOf(labels.getSubs().size()));
        Uri uri = labels.getSubs().get(0);
        holder.mediaThumbnail.getLayoutParams().width = imageSize;
        holder.mediaThumbnail.getLayoutParams().height = imageSize;
        RequestOptions options = new RequestOptions().override(imageSize, imageSize);
        Glide.with(holder.itemView.getContext()).load(uri).apply(options).into(holder.mediaThumbnail);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), LabelListActivity.class);
                Bundle bundle = new Bundle();
                bundle.putParcelableArrayList("list", labels.getSubs());
                intent.putExtras(bundle);
                intent.putExtra("label", labels.getLabel());
                v.getContext().startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return this.labelsList.size();
    }

    public static class MyHolder extends RecyclerView.ViewHolder {
        private ImageView mediaThumbnail;
        private TextView labelTv;
        private TextView labelNum;

        public MyHolder(@NonNull View itemView) {
            super(itemView);
            mediaThumbnail = itemView.findViewById(R.id.media_thumbnail);
            labelTv = itemView.findViewById(R.id.label_text);
            labelNum = itemView.findViewById(R.id.label_text_num);
            labelNum.setVisibility(View.VISIBLE);
            labelTv.setVisibility(View.VISIBLE);
        }
    }
}
