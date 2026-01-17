package com.example.musicplayer.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {User.class, SongEntity.class}, version = 3, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    private static AppDatabase instance;
    private static final String DATABASE_NAME = "music_player_database";
    
    public abstract UserDao userDao();
    public abstract SongDao songDao();
    
    public static synchronized AppDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(
                    context.getApplicationContext(),
                    AppDatabase.class,
                    DATABASE_NAME
            )
            .fallbackToDestructiveMigration() // Pentru simplitate - șterge datele la upgrade
            .allowMainThreadQueries() // For simplicity, in production use background threads
            .build();
        }
        return instance;
    }
    
    /**
     * Returnează calea completă unde este salvată baza de date pe dispozitiv
     * Path: /data/data/com.example.musicplayer/databases/music_player_database
     */
    public static String getDatabasePath(Context context) {
        return context.getDatabasePath(DATABASE_NAME).getAbsolutePath();
    }
}
