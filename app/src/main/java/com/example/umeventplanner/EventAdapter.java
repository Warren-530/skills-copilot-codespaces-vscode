package com.example.umeventplanner;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {

    private Context context;
    private List<Event> eventList;
    private OnEventClickListener onEventClickListener;

    public EventAdapter(Context context, List<Event> eventList, OnEventClickListener onEventClickListener) {
        this.context = context;
        this.eventList = eventList;
        this.onEventClickListener = onEventClickListener;
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_event, parent, false);
        return new EventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        Event event = eventList.get(position);
        
        holder.tvEventTitle.setText(event.getTitle());
        holder.tvEventDate.setText(event.getDate());
        holder.tvEventLocation.setText(event.getLocation());
        holder.tvGreenScore.setText(String.format("%.1f", event.getSustainabilityScore()));

        if (event.getBannerUrl() != null && !event.getBannerUrl().isEmpty()) {
            Glide.with(context)
                .load(event.getBannerUrl())
                .into(holder.ivEventImage);
        }

        holder.itemView.setOnClickListener(v -> {
            if (onEventClickListener != null) {
                onEventClickListener.onEventClick(event);
            }
        });
    }

    @Override
    public int getItemCount() {
        return eventList.size();
    }

    public static class EventViewHolder extends RecyclerView.ViewHolder {

        ImageView ivEventImage;
        TextView tvEventTitle, tvEventDate, tvEventLocation, tvGreenScore;

        public EventViewHolder(@NonNull View itemView) {
            super(itemView);
            ivEventImage = itemView.findViewById(R.id.ivEventImage);
            tvEventTitle = itemView.findViewById(R.id.tvEventTitle);
            tvEventDate = itemView.findViewById(R.id.tvEventDate);
            tvEventLocation = itemView.findViewById(R.id.tvEventLocation);
            tvGreenScore = itemView.findViewById(R.id.tvGreenScore);
        }
    }
}
