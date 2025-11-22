package com.example.umeventplanner;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class PosterCarouselAdapter extends RecyclerView.Adapter<PosterCarouselAdapter.ViewHolder> {

    private Context context;
    private List<String> posterUrls;
    private OnPosterClickListener onPosterClickListener;

    public PosterCarouselAdapter(Context context, List<String> posterUrls, OnPosterClickListener onPosterClickListener) {
        this.context = context;
        this.posterUrls = posterUrls;
        this.onPosterClickListener = onPosterClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_poster_carousel, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String posterUrl = posterUrls.get(position);
        Glide.with(context).load(posterUrl).into(holder.ivPoster);
        holder.itemView.setOnClickListener(v -> {
            if (onPosterClickListener != null) {
                onPosterClickListener.onPosterClick(posterUrl);
            }
        });
    }

    @Override
    public int getItemCount() {
        return posterUrls.size();
    }

    public interface OnPosterClickListener {
        void onPosterClick(String posterUrl);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivPoster;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivPoster = itemView.findViewById(R.id.ivPoster);
        }
    }
}
