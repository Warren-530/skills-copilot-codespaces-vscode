package com.example.umeventplanner;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.umeventplanner.adapters.PosterDisplayAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EventDetailsFragment extends Fragment implements PosterDisplayAdapter.OnPosterClickListener {

    private String eventId;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    // UI Elements
    private ImageView ivDetailBanner;
    private TextView tvDetailTitle, tvDetailDate, tvDetailLocation, tvImpactLabel, tvDetailDescription;
    private com.google.android.material.chip.Chip chipStatus;
    private RecyclerView rvPosterCarousel;
    private RatingBar rbDetailScore;
    private LinearLayout llChecklistContainer;
    private Button btnRegister;

    public static EventDetailsFragment newInstance(String eventId) {
        EventDetailsFragment fragment = new EventDetailsFragment();
        Bundle args = new Bundle();
        args.putString("eventId", eventId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            eventId = getArguments().getString("eventId");
        }
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_event_details, container, false);
        initViews(view);
        if (eventId != null) {
            loadEventDetails();
            checkRegistrationStatus();
        }
        return view;
    }

    private void initViews(View view) {
        ivDetailBanner = view.findViewById(R.id.ivDetailBanner);
        tvDetailTitle = view.findViewById(R.id.tvDetailTitle);
        tvDetailDate = view.findViewById(R.id.tvDetailDate);
        tvDetailLocation = view.findViewById(R.id.tvDetailLocation);
        tvDetailDescription = view.findViewById(R.id.tvDetailDescription);
        chipStatus = view.findViewById(R.id.chipStatus);
        rvPosterCarousel = view.findViewById(R.id.rvPosterCarousel);
        rbDetailScore = view.findViewById(R.id.rbDetailScore);
        tvImpactLabel = view.findViewById(R.id.tvImpactLabel);
        llChecklistContainer = view.findViewById(R.id.llChecklistContainer);
        btnRegister = view.findViewById(R.id.btnRegister);
    }

    private void loadEventDetails() {
        db.collection("events").document(eventId).get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                Event event = documentSnapshot.toObject(Event.class);
                if (event != null && getContext() != null) {
                    if (event.getBannerUrl() != null && !event.getBannerUrl().isEmpty()) {
                        Glide.with(getContext()).load(event.getBannerUrl()).into(ivDetailBanner);
                    }
                    tvDetailTitle.setText(event.getTitle());
                    tvDetailDate.setText(event.getDate());
                    tvDetailLocation.setText(event.getLocation());
                    tvDetailDescription.setText(event.getDescription());
                    chipStatus.setText(event.getStatus());
                    rbDetailScore.setRating(event.getSustainabilityScore());

                    int adoptedPractices = 0;
                    if (event.getChecklist() != null) {
                        for (Boolean isAdopted : event.getChecklist().values()) {
                            if (isAdopted) adoptedPractices++;
                        }
                    }
                    tvImpactLabel.setText(adoptedPractices + "/25 Practices Adopted");

                    if (event.getPosterUrls() != null) {
                        rvPosterCarousel.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
                        PosterDisplayAdapter adapter = new PosterDisplayAdapter(getContext(), event.getPosterUrls(), this);
                        rvPosterCarousel.setAdapter(adapter);
                    }

                    llChecklistContainer.removeAllViews();
                    if (event.getChecklist() != null) {
                        for (Map.Entry<String, Boolean> entry : event.getChecklist().entrySet()) {
                            if (entry.getValue()) {
                                TextView checklistItem = new TextView(getContext());
                                checklistItem.setText("✓ " + formatChecklistItem(entry.getKey()));
                                llChecklistContainer.addView(checklistItem);
                            }
                        }
                    }
                }
            }
        });
    }

    private void checkRegistrationStatus() {
        if (mAuth.getCurrentUser() == null) return;
        String userId = mAuth.getCurrentUser().getUid();
        db.collection("users").document(userId).collection("registrations").document(eventId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        btnRegister.setText("View Ticket");
                        btnRegister.setOnClickListener(v -> {
                            // TODO: Navigate to MyEvents Fragment
                        });
                    } else {
                        btnRegister.setText("Register");
                        btnRegister.setOnClickListener(v -> registerForEvent());
                    }
                });
    }

    private void registerForEvent() {
        String userId = mAuth.getCurrentUser().getUid();
        final DocumentReference eventRef = db.collection("events").document(eventId);
        final DocumentReference userRegistrationRef = db.collection("users").document(userId).collection("registrations").document(eventId);

        db.runTransaction(transaction -> {
            DocumentSnapshot eventSnapshot = transaction.get(eventRef);
            Long maxParticipants = eventSnapshot.getLong("maxParticipants");
            Long currentParticipants = eventSnapshot.getLong("currentParticipants");
            if (currentParticipants == null) currentParticipants = 0L;

            if (maxParticipants != null && currentParticipants < maxParticipants) {
                transaction.update(eventRef, "currentParticipants", currentParticipants + 1);
                Map<String, Object> registrationData = new HashMap<>();
                registrationData.put("registrationTime", com.google.firebase.Timestamp.now());
                transaction.set(userRegistrationRef, registrationData);
                return true;
            } else {
                return false;
            }
        }).addOnSuccessListener(success -> {
            if (success) {
                Toast.makeText(getContext(), "Registered Successfully!", Toast.LENGTH_SHORT).show();
                checkRegistrationStatus(); // Update button state
            } else {
                Toast.makeText(getContext(), "Event is full or registration is not available.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onPosterClick(String imageUrl) {
        showZoomDialog(imageUrl);
    }

    private void showZoomDialog(String imageUrl) {
        if (getContext() == null) return;
        Dialog dialog = new Dialog(getContext(), android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        dialog.setContentView(R.layout.dialog_poster_zoom);
        ImageView ivZoomedPoster = dialog.findViewById(R.id.ivZoomedPoster);
        Glide.with(getContext()).load(imageUrl).into(ivZoomedPoster);
        dialog.findViewById(R.id.btnCloseZoom).setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private String formatChecklistItem(String rawKey) {
        rawKey = rawKey.replace("cb", "");
        StringBuilder formatted = new StringBuilder();
        for (char c : rawKey.toCharArray()) {
            if (Character.isUpperCase(c)) {
                formatted.append(' ');
            }
            formatted.append(c);
        }
        return formatted.toString().trim();
    }
}
