package com.zhihu.matisse.ui.labels;

import android.annotation.SuppressLint;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.zhihu.matisse.R;

import java.util.ArrayList;
import java.util.List;

public class LabelListAdapter extends RecyclerView.Adapter<LabelListAdapter.MyHolder> {

    private final List<Uri> labelsList = new ArrayList<>();

    private int imageSize = 0;

    @SuppressLint("NotifyDataSetChanged")
    public void update(List<Uri> labelsList) {
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
        Uri uri = labelsList.get(position);
        holder.mediaThumbnail.getLayoutParams().width = imageSize;
        holder.mediaThumbnail.getLayoutParams().height = imageSize;
        RequestOptions options = new RequestOptions().override(imageSize, imageSize);
        Glide.with(holder.itemView.getContext()).load(uri).apply(options).into(holder.mediaThumbnail);
    }

    @Override
    public int getItemCount() {
        return this.labelsList.size();
    }

    public static class MyHolder extends RecyclerView.ViewHolder {
        private ImageView mediaThumbnail;

        public MyHolder(@NonNull View itemView) {
            super(itemView);
            mediaThumbnail = itemView.findViewById(R.id.media_thumbnail);
        }
    }
}
