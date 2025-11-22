package com.example.umeventplanner.local;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {EventEntity.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    public abstract EventDao eventDao();
}
