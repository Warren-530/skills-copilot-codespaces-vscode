package com.example.umeventplanner.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.umeventplanner.R;
import java.util.List;

public class PosterDisplayAdapter extends RecyclerView.Adapter<PosterDisplayAdapter.PosterViewHolder> {

    private final Context context;
    private final List<String> posterUrls;
    private final OnPosterClickListener onPosterClickListener;

    public interface OnPosterClickListener {
        void onPosterClick(String imageUrl);
    }

    public PosterDisplayAdapter(Context context, List<String> posterUrls, OnPosterClickListener onPosterClickListener) {
        this.context = context;
        this.posterUrls = posterUrls;
        this.onPosterClickListener = onPosterClickListener;
    }

    @NonNull
    @Override
    public PosterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_poster_display, parent, false);
        return new PosterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PosterViewHolder holder, int position) {
        String imageUrl = posterUrls.get(position);

        Glide.with(context)
                .load(imageUrl)
                .into(holder.ivPosterImage);

        holder.itemView.setOnClickListener(v -> {
            if (onPosterClickListener != null) {
                onPosterClickListener.onPosterClick(imageUrl);
            }
        });
    }

    @Override
    public int getItemCount() {
        return posterUrls.size();
    }

    static class PosterViewHolder extends RecyclerView.ViewHolder {
        ImageView ivPosterImage;

        PosterViewHolder(@NonNull View itemView) {
            super(itemView);
            ivPosterImage = itemView.findViewById(R.id.ivPosterImage);
        }
    }
}
