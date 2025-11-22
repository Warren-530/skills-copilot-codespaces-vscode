package com.example.umeventplanner;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class DiscoverFragment extends Fragment implements OnEventClickListener {

    private RecyclerView rvEvents;
    private EventAdapter eventAdapter;
    private List<Event> eventList;
    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_discover, container, false);

        rvEvents = view.findViewById(R.id.rvEvents);
        rvEvents.setLayoutManager(new LinearLayoutManager(getContext()));
        eventList = new ArrayList<>();
        eventAdapter = new EventAdapter(getContext(), eventList, this);
        rvEvents.setAdapter(eventAdapter);

        db = FirebaseFirestore.getInstance();
        loadEvents();

        return view;
    }

    private void loadEvents() {
        db.collection("events")
                .whereEqualTo("status", "Published")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    eventList.clear();
                    for (com.google.firebase.firestore.DocumentSnapshot document : queryDocumentSnapshots) {
                        Event event = document.toObject(Event.class);
                        if (event != null) {
                            event.setEventId(document.getId());
                            eventList.add(event);
                        }
                    }
                    eventAdapter.notifyDataSetChanged();
                });
    }

    @Override
    public void onEventClick(Event event) {
        getParentFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, EventDetailsFragment.newInstance(event))
                .addToBackStack(null)
                .commit();
    }
}
