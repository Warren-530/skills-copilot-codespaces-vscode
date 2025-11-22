package com.example.umeventplanner.local;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface EventDao {
    @Query("SELECT * FROM events")
    List<EventEntity> getAllEvents();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<EventEntity> events);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(EventEntity event);

    @Query("DELETE FROM events")
    void deleteAll();
}
