package com.example.umeventplanner;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.Map;

public class EventDetailsFragment extends Fragment implements PosterCarouselAdapter.OnPosterClickListener {

    private Event event;

    public static EventDetailsFragment newInstance(Event event) {
        EventDetailsFragment fragment = new EventDetailsFragment();
        Bundle args = new Bundle();
        args.putSerializable("event", event);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            event = (Event) getArguments().getSerializable("event");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_event_details, container, false);

        ImageView ivEventBanner = view.findViewById(R.id.ivEventBanner);
        TextView tvEventTitle = view.findViewById(R.id.tvEventTitle);
        TextView tvEventDate = view.findViewById(R.id.tvEventDate);
        TextView tvEventLocation = view.findViewById(R.id.tvEventLocation);
        TextView tvEventDescription = view.findViewById(R.id.tvEventDescription);
        TextView tvImpact = view.findViewById(R.id.tvImpact);
        RecyclerView rvPosterCarousel = view.findViewById(R.id.rvPosterCarousel);

        if (event != null) {
            Glide.with(this).load(event.getBannerUrl()).into(ivEventBanner);
            tvEventTitle.setText(event.getTitle());
            tvEventDate.setText(event.getDate());
            tvEventLocation.setText(event.getLocation());
            tvEventDescription.setText(event.getDescription());

            int adoptedPractices = 0;
            for (Boolean isAdopted : event.getChecklist().values()) {
                if (isAdopted) {
                    adoptedPractices++;
                }
            }
            tvImpact.setText("Impact: " + adoptedPractices + "/25 sustainable practices adopted.");

            rvPosterCarousel.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
            PosterCarouselAdapter adapter = new PosterCarouselAdapter(getContext(), event.getPosterUrls(), this);
            rvPosterCarousel.setAdapter(adapter);
        }

        return view;
    }

    @Override
    public void onPosterClick(String posterUrl) {
        FullScreenImageDialog.newInstance(posterUrl).show(getParentFragmentManager(), "full_screen_image");
    }
}
