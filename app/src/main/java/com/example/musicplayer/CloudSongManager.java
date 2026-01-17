package com.example.musicplayer;

import android.content.Context;
import android.widget.Toast;

import com.example.musicplayer.database.AppDatabase;
import com.example.musicplayer.database.SongEntity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Manager pentru melodiile stocate în cloud (Firestore)
 * Melodiile în sine sunt stocate într-un serviciu extern gratuit (Google Drive, Dropbox, etc.)
 */
public class CloudSongManager {
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private Context context;

    public CloudSongManager(Context context) {
        this.context = context;
        this.db = FirebaseFirestore.getInstance();
        this.mAuth = FirebaseAuth.getInstance();
    }

    /**
     * Adaugă o melodie cloud în Firestore
     * @param cloudUrl URL-ul către melodia stocată în cloud (Google Drive, Dropbox, etc.)
     * @param title Titlul melodiei
     * @param artist Artistul
     * @param genre Genul
     * @param coverImageUrl URL-ul către poza de cover (opțional)
     */
    public void addCloudSong(String cloudUrl, String title, String artist, String genre, String coverImageUrl, OnCloudSongAdded callback) {
        String userId = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : null;
        if (userId == null) {
            callback.onError("User not logged in");
            return;
        }

        // Verifică dacă URL-ul este valid
        if (cloudUrl == null || cloudUrl.trim().isEmpty() || !cloudUrl.startsWith("http")) {
            callback.onError("Invalid URL. Please provide a valid HTTP/HTTPS URL.");
            return;
        }

        Map<String, Object> songData = new HashMap<>();
        songData.put("cloudUrl", cloudUrl);
        songData.put("title", title);
        songData.put("artist", artist);
        songData.put("genre", genre);
        songData.put("userId", userId);
        songData.put("createdAt", System.currentTimeMillis());
        songData.put("isCloudSong", true);
        if (coverImageUrl != null && !coverImageUrl.trim().isEmpty()) {
            songData.put("coverImageUrl", coverImageUrl);
        }

        db.collection("cloud_songs")
            .add(songData)
            .addOnSuccessListener(documentReference -> {
                // Adaugă și în baza de date locală pentru acces rapid
                addToLocalDatabase(cloudUrl, title, artist, genre, coverImageUrl);
                callback.onSuccess(documentReference.getId());
            })
            .addOnFailureListener(e -> {
                callback.onError("Failed to add song: " + e.getMessage());
            });
    }

    /**
     * Obține toate melodiile cloud ale utilizatorului
     */
    public void getUserCloudSongs(OnCloudSongsLoaded callback) {
        String userId = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : null;
        if (userId == null) {
            callback.onError("User not logged in");
            return;
        }

        db.collection("cloud_songs")
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                List<Song> cloudSongs = new ArrayList<>();
                for (var doc : queryDocumentSnapshots.getDocuments()) {
                    String cloudUrl = doc.getString("cloudUrl");
                    String title = doc.getString("title");
                    String artist = doc.getString("artist");
                    String genre = doc.getString("genre");
                    String coverImageUrl = doc.getString("coverImageUrl");
                    
                    if (cloudUrl != null && title != null) {
                        Song song;
                        if (coverImageUrl != null && !coverImageUrl.isEmpty()) {
                            song = new Song(cloudUrl, title, artist != null ? artist : "Unknown", genre != null ? genre : "Unknown", coverImageUrl);
                        } else {
                            song = new Song(cloudUrl, title, artist != null ? artist : "Unknown", genre != null ? genre : "Unknown");
                        }
                        cloudSongs.add(song);
                    }
                }
                callback.onSuccess(cloudSongs);
            })
            .addOnFailureListener(e -> {
                callback.onError("Failed to load cloud songs: " + e.getMessage());
            });
    }

    /**
     * Adaugă melodia în baza de date locală pentru acces rapid
     */
    private void addToLocalDatabase(String cloudUrl, String title, String artist, String genre, String coverImageUrl) {
        AppDatabase database = AppDatabase.getInstance(context);
        SongEntity entity;
        if (coverImageUrl != null && !coverImageUrl.trim().isEmpty()) {
            entity = new SongEntity(cloudUrl, title, artist, genre, coverImageUrl);
        } else {
            entity = new SongEntity(cloudUrl, title, artist, genre);
        }
        entity.setCoverImageUrl(coverImageUrl);
        database.songDao().insertSong(entity);
    }

    /**
     * Șterge o melodie cloud
     */
    public void deleteCloudSong(String songId, OnCloudSongDeleted callback) {
        db.collection("cloud_songs").document(songId)
            .delete()
            .addOnSuccessListener(aVoid -> {
                callback.onSuccess();
            })
            .addOnFailureListener(e -> {
                callback.onError("Failed to delete song: " + e.getMessage());
            });
    }

    public interface OnCloudSongAdded {
        void onSuccess(String songId);
        void onError(String error);
    }

    public interface OnCloudSongsLoaded {
        void onSuccess(List<Song> songs);
        void onError(String error);
    }

    public interface OnCloudSongDeleted {
        void onSuccess();
        void onError(String error);
    }
}
