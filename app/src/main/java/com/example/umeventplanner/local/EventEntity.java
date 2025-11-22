package com.example.umeventplanner.local;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.annotation.NonNull;

@Entity(tableName = "events")
public class EventEntity {
    @PrimaryKey
    @NonNull
    public String id;
    public String eventName;
    public String description;
    public String eventDate;
    public String time;
    public String location;
    public double sustainabilityScore;
    public String impact;
    public String organizerId;

    public EventEntity(@NonNull String id, String eventName, String description, String eventDate, String time, String location, double sustainabilityScore, String impact, String organizerId) {
        this.id = id;
        this.eventName = eventName;
        this.description = description;
        this.eventDate = eventDate;
        this.time = time;
        this.location = location;
        this.sustainabilityScore = sustainabilityScore;
        this.impact = impact;
        this.organizerId = organizerId;
    }
}
