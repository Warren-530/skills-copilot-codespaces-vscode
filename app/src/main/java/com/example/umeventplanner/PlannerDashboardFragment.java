package com.example.umeventplanner;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import java.util.Date;

public class PlannerDashboardFragment extends Fragment {

    private TextView tvWelcome, tvTotalEvents, tvAvgScore, tvResourcesSaved;
    private TextView tvUpcomingEventTitle, tvUpcomingEventDate, tvUpcomingEventTime, tvUpcomingEventLocation, tvNoEvents;
    private Button btnCreateEvent;
    private CardView cardUpcomingEvent;
    private ProgressBar progressBar;

    private FirebaseFirestore db;
    private FirebaseUser currentUser;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_planner_dashboard, container, false);

        db = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        // Initialize Views
        tvWelcome = view.findViewById(R.id.tvWelcome);
        tvTotalEvents = view.findViewById(R.id.tvTotalEvents);
        tvAvgScore = view.findViewById(R.id.tvAvgScore);
        tvResourcesSaved = view.findViewById(R.id.tvResourcesSaved);
        tvUpcomingEventTitle = view.findViewById(R.id.tvUpcomingEventTitle);
        tvUpcomingEventDate = view.findViewById(R.id.tvUpcomingEventDate);
        tvUpcomingEventTime = view.findViewById(R.id.tvUpcomingEventTime);
        tvUpcomingEventLocation = view.findViewById(R.id.tvUpcomingEventLocation);
        tvNoEvents = view.findViewById(R.id.tvNoEvents);
        btnCreateEvent = view.findViewById(R.id.btnCreateEvent);
        cardUpcomingEvent = view.findViewById(R.id.cardUpcomingEvent);
        progressBar = view.findViewById(R.id.progressBar);

        btnCreateEvent.setOnClickListener(v -> getParentFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new CreateEventFragment())
                .addToBackStack(null)
                .commit());

        loadDashboardData();

        return view;
    }

    private void loadDashboardData() {
        progressBar.setVisibility(View.VISIBLE);

        if (currentUser != null) {
            // Load User Stats
            db.collection("users").document(currentUser.getUid()).get().addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    String name = documentSnapshot.getString("name");
                    tvWelcome.setText("Welcome, " + name + "!");

                    if (documentSnapshot.contains("myImpact")) {
                        DocumentSnapshot impactMap = (DocumentSnapshot) documentSnapshot.get("myImpact");
                        if (impactMap != null && impactMap.exists()) {
                            tvTotalEvents.setText(String.valueOf(impactMap.getLong("eventsCount")));
                            tvAvgScore.setText(String.format("%.1f", impactMap.getDouble("avgScore")));
                            // Assuming 'resourcesSaved' is a numeric value. Formatting may be needed.
                            tvResourcesSaved.setText(String.valueOf(impactMap.getLong("resourcesSaved")));
                        }
                    }
                }
            });

            // Load Upcoming Event
            db.collection("events")
                    .whereArrayContains("plannerUIDs", currentUser.getUid())
                    .whereGreaterThanOrEqualTo("date", new Date())
                    .orderBy("date", Query.Direction.ASCENDING)
                    .limit(1)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            DocumentSnapshot eventDoc = queryDocumentSnapshots.getDocuments().get(0);
                            Event event = eventDoc.toObject(Event.class);
                            if (event != null) {
                                tvUpcomingEventTitle.setText(event.getTitle());
                                // Assuming date is a string that needs parsing for time
                                String[] dateTime = event.getDate().split(" ");
                                tvUpcomingEventDate.setText(dateTime.length > 0 ? dateTime[0] : "");
                                tvUpcomingEventTime.setText(dateTime.length > 1 ? dateTime[1] : "");
                                tvUpcomingEventLocation.setText(event.getLocation());
                                cardUpcomingEvent.setVisibility(View.VISIBLE);
                                tvNoEvents.setVisibility(View.GONE);
                            }
                        } else {
                            tvNoEvents.setVisibility(View.VISIBLE);
                            cardUpcomingEvent.setVisibility(View.GONE);
                        }
                        progressBar.setVisibility(View.GONE);
                    })
                    .addOnFailureListener(e -> {
                        progressBar.setVisibility(View.GONE);
                        tvNoEvents.setVisibility(View.VISIBLE);
                        cardUpcomingEvent.setVisibility(View.GONE);
                    });
        }
    }
}
