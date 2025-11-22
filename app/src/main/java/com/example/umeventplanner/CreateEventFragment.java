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

import com.google.android.gms.tasks.Tasks;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class CreateEventFragment extends Fragment {

    private EditText etEventTitle, etEventDesc, etEventDate, etStartTime, etEndTime, etLocation, etMaxParticipants, etCollaboratorId;
    private ImageView ivEventBanner;
    private Button btnSelectBanner, btnSelectPoster, btnAddCollaborator, btnPublishEvent, btnSaveDraft;
    private TextView tvCollaboratorList, tvScoreLabel;
    private RatingBar rbSustainabilityScore;
    private ProgressBar progressBar;
    private RecyclerView rvSelectedPostersPreview;

    private final List<String> collaboratorIds = new ArrayList<>();
    private Uri bannerUri;
    private final List<Uri> selectedPosterUris = new ArrayList<>();
    private SelectedPosterAdapter posterPreviewAdapter;
    private String currentPickerRequest;

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
                            if (selectedPosterUris.size() + count > 5) {
                                Toast.makeText(getContext(), "Cannot select more than 5 posters", Toast.LENGTH_SHORT).show();
                                return;
                            }
                            for (int i = 0; i < count; i++) {
                                selectedPosterUris.add(clipData.getItemAt(i).getUri());
                            }
                        } else if (result.getData().getData() != null) {
                            if (selectedPosterUris.size() >= 5) {
                                Toast.makeText(getContext(), "Cannot select more than 5 posters", Toast.LENGTH_SHORT).show();
                                return;
                            }
                            selectedPosterUris.add(result.getData().getData());
                        }
                        posterPreviewAdapter.notifyDataSetChanged();
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
        rvSelectedPostersPreview = view.findViewById(R.id.rvSelectedPostersPreview);
        btnSelectPoster = view.findViewById(R.id.btnSelectPoster);
        etCollaboratorId = view.findViewById(R.id.etCollaboratorId);
        btnAddCollaborator = view.findViewById(R.id.btnAddCollaborator);
        tvCollaboratorList = view.findViewById(R.id.tvCollaboratorList);
        rbSustainabilityScore = view.findViewById(R.id.rbSustainabilityScore);
        tvScoreLabel = view.findViewById(R.id.tvScoreLabel);
        progressBar = view.findViewById(R.id.progressBar);
        btnPublishEvent = view.findViewById(R.id.btnPublishEvent);
        btnSaveDraft = view.findViewById(R.id.btnSaveDraft);

        posterPreviewAdapter = new SelectedPosterAdapter(selectedPosterUris);
        rvSelectedPostersPreview.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        rvSelectedPostersPreview.setAdapter(posterPreviewAdapter);

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
        btnSelectPoster.setOnClickListener(v -> openGalleryFor("poster"));
        btnAddCollaborator.setOnClickListener(v -> addCollaborator());
        btnPublishEvent.setOnClickListener(v -> handlePublish("Published"));
        btnSaveDraft.setOnClickListener(v -> handlePublish("Draft"));
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
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
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
        rbSustainabilityScore.setRating(score);
        tvScoreLabel.setText(String.format("Sustainability Score: %.1f/5.0", score));
    }

    private void handlePublish(String status) {
        if (TextUtils.isEmpty(etEventTitle.getText()) || TextUtils.isEmpty(etEventDate.getText()) || TextUtils.isEmpty(etLocation.getText())) {
            Toast.makeText(getContext(), "Title, Date, and Location are required.", Toast.LENGTH_SHORT).show();
            return;
        }
        progressBar.setVisibility(View.VISIBLE);
        btnPublishEvent.setEnabled(false);
        btnSaveDraft.setEnabled(false);
        uploadImagesAndSaveEvent(status);
    }

    private void uploadImagesAndSaveEvent(String status) {
        final String eventId = UUID.randomUUID().toString();
        StorageReference storageRef = FirebaseStorage.getInstance().getReference();

        final String[] bannerUrl = {null};

        if (bannerUri != null) {
            final StorageReference bannerRef = storageRef.child("event_images/" + eventId + "_banner.jpg");
            bannerRef.putFile(bannerUri).addOnSuccessListener(taskSnapshot -> bannerRef.getDownloadUrl().addOnSuccessListener(uri -> {
                bannerUrl[0] = uri.toString();
                uploadPostersAndSave(status, eventId, bannerUrl[0]);
            }));
        } else {
            uploadPostersAndSave(status, eventId, null);
        }
    }

    private void uploadPostersAndSave(String status, String eventId, String bannerUrl) {
        StorageReference storageRef = FirebaseStorage.getInstance().getReference();
        List<com.google.android.gms.tasks.Task<Uri>> uploadTasks = new ArrayList<>();
        if (selectedPosterUris.isEmpty()) {
            saveEventToFirestore(status, eventId, bannerUrl, new ArrayList<>());
            return;
        }
        for (int i = 0; i < selectedPosterUris.size(); i++) {
            Uri posterUri = selectedPosterUris.get(i);
            final StorageReference posterRef = storageRef.child("event_posters/" + eventId + "_" + System.currentTimeMillis() + "_" + i + ".jpg");
            uploadTasks.add(posterRef.putFile(posterUri).continueWithTask(task -> posterRef.getDownloadUrl()));
        }

        Tasks.whenAllSuccess(uploadTasks).addOnSuccessListener(urls -> {
            List<String> finalPosterUrls = new ArrayList<>();
            for (Object url : urls) {
                finalPosterUrls.add(url.toString());
            }
            saveEventToFirestore(status, eventId, bannerUrl, finalPosterUrls);
        });
    }

    private void saveEventToFirestore(String status, String eventId, String bannerUrl, List<String> posterUrls) {
        Map<String, Object> event = new HashMap<>();
        event.put("title", etEventTitle.getText().toString());
        event.put("description", etEventDesc.getText().toString());
        event.put("date", etEventDate.getText().toString());
        event.put("startTime", etStartTime.getText().toString());
        event.put("endTime", etEndTime.getText().toString());
        event.put("location", etLocation.getText().toString());
        event.put("maxParticipants", Integer.parseInt(etMaxParticipants.getText().toString()));
        event.put("bannerUrl", bannerUrl);
        event.put("posterUrls", posterUrls);
        event.put("sustainabilityScore", rbSustainabilityScore.getRating());

        Map<String, Boolean> checklist = new HashMap<>();
        for (int id : CHECKBOX_IDS) {
            CheckBox cb = getView().findViewById(id);
            if (cb != null) {
                checklist.put(getResources().getResourceEntryName(id), cb.isChecked());
            }
        }
        event.put("checklist", checklist);
        event.put("status", status);

        List<String> plannerUIDs = new ArrayList<>(collaboratorIds);
        plannerUIDs.add(FirebaseAuth.getInstance().getCurrentUser().getUid());
        event.put("plannerUIDs", plannerUIDs);
        event.put("createdAt", Timestamp.now());

        FirebaseFirestore.getInstance().collection("events").document(eventId).set(event).addOnSuccessListener(aVoid -> {
            progressBar.setVisibility(View.GONE);
            Toast.makeText(getContext(), "Event " + status, Toast.LENGTH_SHORT).show();
            getParentFragmentManager().popBackStack();
        });
    }
    
    public class SelectedPosterAdapter extends RecyclerView.Adapter<SelectedPosterAdapter.ViewHolder> {
        private List<Uri> posters;

        public SelectedPosterAdapter(List<Uri> posters) {
            this.posters = posters;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_selected_poster_thumbnail, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            holder.ivPosterThumbnail.setImageURI(posters.get(position));
            holder.btnRemovePoster.setOnClickListener(v -> {
                posters.remove(position);
                notifyDataSetChanged();
            });
        }

        @Override
        public int getItemCount() {
            return posters.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            ImageView ivPosterThumbnail;
            ImageView btnRemovePoster;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                ivPosterThumbnail = itemView.findViewById(R.id.ivPosterThumbnail);
                btnRemovePoster = itemView.findViewById(R.id.btnRemovePoster);
            }
        }
    }

    private static final int[] CHECKBOX_IDS = {
            R.id.cbDigitalTickets, R.id.cbPaperlessMaterials, R.id.cbDigitalFeedback, R.id.cbHybridOption, R.id.cbSmartNotifs,
            R.id.cbNoPlastic, R.id.cbByoContainer, R.id.cbPlantBased, R.id.cbSmartCatering, R.id.cbFoodRecovery,
            R.id.cbPublicTransport, R.id.cbNaturalLight, R.id.cbEcoTemp, R.id.cbPowerDown, R.id.cbCarbonOffset,
            R.id.cbDigitalPromo, R.id.cbReusableSignage, R.id.cbSustSwag, R.id.cbBadgeReturn, R.id.cbRecyclingBins,
            R.id.cbWheelchair, R.id.cbGenderNeutral, R.id.cbDiverseLineup, R.id.cbLocalSourcing, R.id.cbWellnessArea
    };
}
