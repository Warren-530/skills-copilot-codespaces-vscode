package com.example.umeventplanner;

import com.google.firebase.Timestamp;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Event implements Serializable {

    private String eventId;
    private String title;
    private String description;
    private String date;
    private String startTime;
    private String endTime;
    private String location;
    private int maxParticipants;
    private String bannerUrl;
    private List<String> posterUrls;
    private float sustainabilityScore;
    private Map<String, Boolean> checklist;
    private String status;
    private List<String> plannerUIDs;
    private Timestamp createdAt;

    public Event() {
        // Default constructor for Firestore
        this.posterUrls = new ArrayList<>();
        this.plannerUIDs = new ArrayList<>();
        this.checklist = new HashMap<>();
    }

    // Getters
    public String getEventId() { return eventId; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getDate() { return date; }
    public String getStartTime() { return startTime; }
    public String getEndTime() { return endTime; }
    public String getLocation() { return location; }
    public int getMaxParticipants() { return maxParticipants; }
    public String getBannerUrl() { return bannerUrl; }
    public List<String> getPosterUrls() { return posterUrls; }
    public float getSustainabilityScore() { return sustainabilityScore; }
    public Map<String, Boolean> getChecklist() { return checklist; }
    public String getStatus() { return status; }
    public List<String> getPlannerUIDs() { return plannerUIDs; }
    public Timestamp getCreatedAt() { return createdAt; }

    // Setters
    public void setEventId(String eventId) { this.eventId = eventId; }
    public void setTitle(String title) { this.title = title; }
    public void setDescription(String description) { this.description = description; }
    public void setDate(String date) { this.date = date; }
    public void setStartTime(String startTime) { this.startTime = startTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }
    public void setLocation(String location) { this.location = location; }
    public void setMaxParticipants(int maxParticipants) { this.maxParticipants = maxParticipants; }
    public void setBannerUrl(String bannerUrl) { this.bannerUrl = bannerUrl; }
    public void setPosterUrls(List<String> posterUrls) { this.posterUrls = posterUrls; }
    public void setSustainabilityScore(float sustainabilityScore) { this.sustainabilityScore = sustainabilityScore; }
    public void setChecklist(Map<String, Boolean> checklist) { this.checklist = checklist; }
    public void setStatus(String status) { this.status = status; }
    public void setPlannerUIDs(List<String> plannerUIDs) { this.plannerUIDs = plannerUIDs; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
}
