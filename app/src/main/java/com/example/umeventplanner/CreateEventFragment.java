package com.example.umeventplanner;

import android.Manifest;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.ClipData;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.example.umeventplanner.adapters.SelectedPosterAdapter;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;

public class CreateEventFragment extends Fragment implements SelectedPosterAdapter.OnPosterRemoveListener {

    private static final String TAG = "CreateEventFragment";

    // UI Components
    private EditText etEventTitle, etEventDesc, etEventDate, etStartTime, etEndTime, etLocation, etMaxParticipants, etCollaboratorId;
    private ImageView ivEventBanner;
    private Button btnSelectBanner, btnSelectPosters, btnAddCollaborator, btnPublish;
    private TextView tvCollaboratorList, tvScoreLabel;
    private RatingBar rbScore;
    private ProgressBar progressBar;
    private RecyclerView rvPosterPreviews;

    // Data
    private final List<String> collaboratorIds = new ArrayList<>();
    private Uri bannerUri;
    private final List<Uri> posterUris = new ArrayList<>();
    private SelectedPosterAdapter adapter;
    private String currentPickerRequest;

    // Activity Result Launchers
    private final ActivityResultLauncher<String> requestPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    openGalleryFor(currentPickerRequest);
                }
            });

    private final ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    if ("banner".equals(currentPickerRequest)) {
                        bannerUri = result.getData().getData();
                        ivEventBanner.setImageURI(bannerUri);
                    } else if ("poster".equals(currentPickerRequest)) {
                        if (result.getData().getClipData() != null) {
                            ClipData clipData = result.getData().getClipData();
                            int count = clipData.getItemCount();
                            if (posterUris.size() + count > 5) {
                                Toast.makeText(getContext(), "Max 5 posters allowed", Toast.LENGTH_SHORT).show();
                                return;
                            }
                            for (int i = 0; i < count; i++) {
                                posterUris.add(clipData.getItemAt(i).getUri());
                            }
                        } else if (result.getData().getData() != null) {
                            if (posterUris.size() >= 5) {
                                Toast.makeText(getContext(), "Max 5 posters allowed", Toast.LENGTH_SHORT).show();
                                return;
                            }
                            posterUris.add(result.getData().getData());
                        }
                        adapter.notifyDataSetChanged();
                    }
                }
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_create_event, container, false);
        initViews(view);
        setupListeners();
        return view;
    }

    private void initViews(View view) {
        etEventTitle = view.findViewById(R.id.etEventTitle);
        etEventDesc = view.findViewById(R.id.etEventDesc);
        etEventDate = view.findViewById(R.id.etEventDate);
        etStartTime = view.findViewById(R.id.etStartTime);
        etEndTime = view.findViewById(R.id.etEndTime);
        etLocation = view.findViewById(R.id.etLocation);
        etMaxParticipants = view.findViewById(R.id.etMaxParticipants);
        ivEventBanner = view.findViewById(R.id.ivEventBanner);
        btnSelectBanner = view.findViewById(R.id.btnSelectBanner);
        rvPosterPreviews = view.findViewById(R.id.rvPosterPreviews);
        btnSelectPosters = view.findViewById(R.id.btnSelectPosters);
        etCollaboratorId = view.findViewById(R.id.etCollaboratorId);
        btnAddCollaborator = view.findViewById(R.id.btnAddCollaborator);
        tvCollaboratorList = view.findViewById(R.id.tvCollaboratorList);
        rbScore = view.findViewById(R.id.rbScore);
        tvScoreLabel = view.findViewById(R.id.tvScoreLabel);
        progressBar = view.findViewById(R.id.progressBar);
        btnPublish = view.findViewById(R.id.btnPublish);

        adapter = new SelectedPosterAdapter(getContext(), posterUris, this);
        rvPosterPreviews.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        rvPosterPreviews.setAdapter(adapter);

        for (int id : CHECKBOX_IDS) {
            CheckBox cb = view.findViewById(id);
            if (cb != null) {
                cb.setOnCheckedChangeListener((buttonView, isChecked) -> calculateScore());
            }
        }
    }

    private void setupListeners() {
        etEventDate.setOnClickListener(v -> showDatePicker());
        etStartTime.setOnClickListener(v -> showTimePicker(etStartTime));
        etEndTime.setOnClickListener(v -> showTimePicker(etEndTime));
        btnSelectBanner.setOnClickListener(v -> openGalleryFor("banner"));
        btnSelectPosters.setOnClickListener(v -> openGalleryFor("poster"));
        btnAddCollaborator.setOnClickListener(v -> addCollaborator());
        btnPublish.setOnClickListener(v -> handlePublish());
    }

    private void showDatePicker() {
        final Calendar c = Calendar.getInstance();
        new DatePickerDialog(getContext(), (view, year, month, day) ->
                etEventDate.setText(day + "/" + (month + 1) + "/" + year), c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void showTimePicker(final EditText timeField) {
        final Calendar c = Calendar.getInstance();
        new TimePickerDialog(getContext(), (view, hour, minute) ->
                timeField.setText(String.format("%02d:%02d", hour, minute)), c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), false).show();
    }

    private void openGalleryFor(String pickerType) {
        this.currentPickerRequest = pickerType;
        String permission = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU ? Manifest.permission.READ_MEDIA_IMAGES : Manifest.permission.READ_EXTERNAL_STORAGE;
        if (ContextCompat.checkSelfPermission(getContext(), permission) == PackageManager.PERMISSION_GRANTED) {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            if ("poster".equals(pickerType)) {
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
            }
            imagePickerLauncher.launch(intent);
        } else {
            requestPermissionLauncher.launch(permission);
        }
    }

    private void addCollaborator() {
        String collaboratorId = etCollaboratorId.getText().toString().trim();
        if (!collaboratorId.isEmpty()) {
            collaboratorIds.add(collaboratorId);
            tvCollaboratorList.setText(String.join(", ", collaboratorIds));
            etCollaboratorId.setText("");
        }
    }

    private void calculateScore() {
        int checkedCount = 0;
        for (int id : CHECKBOX_IDS) {
            CheckBox cb = getView().findViewById(id);
            if (cb != null && cb.isChecked()) checkedCount++;
        }
        float score = (float) (checkedCount / 25.0) * 5.0f;
        rbScore.setRating(score);
        tvScoreLabel.setText(String.format("Sustainability Score: %.1f/5.0", score));
    }

    private void handlePublish() {
        if (TextUtils.isEmpty(etEventTitle.getText())) {
            Toast.makeText(getContext(), "Title is required.", Toast.LENGTH_SHORT).show();
            return;
        }
        setLoading(true);
        uploadBannerImage();
    }

    private void uploadBannerImage() {
        if (bannerUri == null) {
            Log.d(TAG, "No banner image. Proceeding with posters.");
            uploadPosterImages(null);
            return;
        }

        Log.d(TAG, "Uploading banner to Cloudinary...");
        MediaManager.get().upload(bannerUri).unsigned("MAD Assignment").callback(new UploadCallback() {
            @Override
            public void onSuccess(String requestId, Map resultData) {
                String bannerUrl = (String) resultData.get("secure_url");
                Log.d(TAG, "Banner uploaded. URL: " + bannerUrl);
                uploadPosterImages(bannerUrl);
            }
            @Override
            public void onError(String requestId, ErrorInfo error) {
                Log.e(TAG, "Banner upload failed: " + error.getDescription());
                Toast.makeText(getContext(), "Banner upload failed.", Toast.LENGTH_SHORT).show();
                setLoading(false);
            }
            @Override public void onStart(String requestId) { }
            @Override public void onProgress(String requestId, long bytes, long totalBytes) { }
            @Override public void onReschedule(String requestId, ErrorInfo error) { }
        }).dispatch();
    }

    private void uploadPosterImages(String bannerUrl) {
        if (posterUris.isEmpty()) {
            Log.d(TAG, "No posters to upload. Saving to Firestore.");
            saveEventToFirestore(bannerUrl, new ArrayList<>());
            return;
        }

        final List<String> uploadedPosterUrls = new ArrayList<>();
        final CountDownLatch latch = new CountDownLatch(posterUris.size());

        for (Uri posterUri : posterUris) {
            MediaManager.get().upload(posterUri).unsigned("MAD Assignment").callback(new UploadCallback() {
                @Override
                public void onSuccess(String requestId, Map resultData) {
                    String posterUrl = (String) resultData.get("secure_url");
                    uploadedPosterUrls.add(posterUrl);
                    latch.countDown();
                }
                @Override
                public void onError(String requestId, ErrorInfo error) {
                    Log.e(TAG, "A poster upload failed: " + error.getDescription());
                    latch.countDown(); // Still countdown to not block forever
                }
                @Override public void onStart(String requestId) { }
                @Override public void onProgress(String requestId, long bytes, long totalBytes) { }
                @Override public void onReschedule(String requestId, ErrorInfo error) { }
            }).dispatch();
        }

        new Thread(() -> {
            try {
                latch.await(); // Wait for all uploads to finish
                if(getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        if (uploadedPosterUrls.size() == posterUris.size()) {
                            Log.d(TAG, "All posters uploaded. Saving to Firestore.");
                            saveEventToFirestore(bannerUrl, uploadedPosterUrls);
                        } else {
                            Toast.makeText(getContext(), "One or more posters failed to upload.", Toast.LENGTH_SHORT).show();
                            setLoading(false);
                        }
                    });
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void saveEventToFirestore(String bannerUrl, List<String> posterUrls) {
        final String eventId = UUID.randomUUID().toString();
        Log.d(TAG, "Saving event to Firestore with eventId: " + eventId);

        Map<String, Object> eventData = new HashMap<>();
        eventData.put("title", etEventTitle.getText().toString());
        eventData.put("description", etEventDesc.getText().toString());
        eventData.put("date", etEventDate.getText().toString());
        eventData.put("startTime", etStartTime.getText().toString());
        eventData.put("endTime", etEndTime.getText().toString());
        eventData.put("location", etLocation.getText().toString());
        eventData.put("maxParticipants", Integer.parseInt(etMaxParticipants.getText().toString()));
        eventData.put("bannerUrl", bannerUrl);
        eventData.put("posterUrls", posterUrls);
        eventData.put("sustainabilityScore", rbScore.getRating());
        Map<String, Boolean> checklist = new HashMap<>();
        for (int id : CHECKBOX_IDS) {
            CheckBox cb = getView().findViewById(id);
            if (cb != null) {
                checklist.put(getResources().getResourceEntryName(id), cb.isChecked());
            }
        }
        eventData.put("checklist", checklist);
        eventData.put("status", "Published");
        List<String> plannerUIDs = new ArrayList<>(collaboratorIds);
        plannerUIDs.add(FirebaseAuth.getInstance().getCurrentUser().getUid());
        eventData.put("plannerUIDs", plannerUIDs);
        eventData.put("createdAt", Timestamp.now());

        FirebaseFirestore.getInstance().collection("events").document(eventId).set(eventData)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Event successfully saved!");
                    Toast.makeText(getContext(), "Event Published!", Toast.LENGTH_SHORT).show();
                    setLoading(false);
                    if (getParentFragmentManager() != null) {
                        getParentFragmentManager().popBackStack();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to save event to Firestore.", e);
                    Toast.makeText(getContext(), "Error saving event: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    setLoading(false);
                });
    }

    private void setLoading(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        btnPublish.setEnabled(!isLoading);
    }

    @Override
    public void onPosterRemoved(int position) {
        posterUris.remove(position);
        adapter.notifyItemRemoved(position);
        adapter.notifyItemRangeChanged(position, posterUris.size());
    }

    private static final int[] CHECKBOX_IDS = {
            R.id.cbDigitalTickets, R.id.cbPaperlessMaterials, R.id.cbDigitalFeedback, R.id.cbHybridOption, R.id.cbSmartNotifs,
            R.id.cbNoPlastic, R.id.cbByoContainer, R.id.cbPlantBased, R.id.cbSmartCatering, R.id.cbFoodRecovery,
            R.id.cbPublicTransport, R.id.cbNaturalLight, R.id.cbEcoTemp, R.id.cbPowerDown, R.id.cbCarbonOffset,
            R.id.cbDigitalPromo, R.id.cbReusableSignage, R.id.cbSustSwag, R.id.cbBadgeReturn, R.id.cbRecyclingBins,
            R.id.cbWheelchair, R.id.cbGenderNeutral, R.id.cbDiverseLineup, R.id.cbLocalSourcing, R.id.cbWellnessArea
    };
}
