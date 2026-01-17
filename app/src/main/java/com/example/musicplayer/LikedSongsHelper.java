package com.example.musicplayer;

import android.content.Context;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Helper class pentru gestionarea playlist-ului "Liked Songs"
 */
public class LikedSongsHelper {
    private static final String LIKED_SONGS_PLAYLIST_NAME = "Liked Songs";
    
    /**
     * Obține sau creează playlist-ul "Liked Songs" pentru utilizator
     */
    public static void getOrCreateLikedSongsPlaylist(String userId, OnLikedPlaylistReady callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        
        // Caută playlist-ul "Liked Songs"
        db.collection("playlists")
            .whereEqualTo("userId", userId)
            .whereEqualTo("name", LIKED_SONGS_PLAYLIST_NAME)
            .limit(1)
            .get()
            .addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult() != null && !task.getResult().isEmpty()) {
                    // Playlist-ul există deja
                    QueryDocumentSnapshot doc = (QueryDocumentSnapshot) task.getResult().getDocuments().get(0);
                    Playlist playlist = new Playlist();
                    playlist.setId(doc.getId());
                    playlist.setName(doc.getString("name"));
                    playlist.setUserId(doc.getString("userId"));
                    
                    @SuppressWarnings("unchecked")
                    List<String> songIds = (List<String>) doc.get("songIds");
                    if (songIds != null) {
                        playlist.setSongIds(songIds);
                    }
                    
                    @SuppressWarnings("unchecked")
                    List<String> cloudSongIds = (List<String>) doc.get("cloudSongIds");
                    if (cloudSongIds != null) {
                        playlist.setCloudSongIds(cloudSongIds);
                    }
                    
                    callback.onReady(playlist);
                } else {
                    // Creează playlist-ul "Liked Songs"
                    createLikedSongsPlaylist(userId, callback);
                }
            });
    }
    
    /**
     * Creează playlist-ul "Liked Songs"
     */
    private static void createLikedSongsPlaylist(String userId, OnLikedPlaylistReady callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        
        Map<String, Object> playlist = new HashMap<>();
        playlist.put("name", LIKED_SONGS_PLAYLIST_NAME);
        playlist.put("userId", userId);
        playlist.put("songIds", new ArrayList<String>());
        playlist.put("cloudSongIds", new ArrayList<String>());
        playlist.put("createdAt", System.currentTimeMillis());
        
        db.collection("playlists")
            .add(playlist)
            .addOnSuccessListener(documentReference -> {
                Playlist likedPlaylist = new Playlist(LIKED_SONGS_PLAYLIST_NAME, userId);
                likedPlaylist.setId(documentReference.getId());
                callback.onReady(likedPlaylist);
            })
            .addOnFailureListener(e -> {
                callback.onError("Failed to create Liked Songs playlist");
            });
    }
    
    /**
     * Adaugă sau șterge o melodie din "Liked Songs"
     */
    public static void toggleLikeSong(Context context, Song song, OnLikeToggled callback) {
        String userId = FirebaseAuth.getInstance().getCurrentUser() != null ? 
            FirebaseAuth.getInstance().getCurrentUser().getUid() : null;
        
        if (userId == null) {
            callback.onError("User not logged in");
            return;
        }
        
        getOrCreateLikedSongsPlaylist(userId, new OnLikedPlaylistReady() {
            @Override
            public void onReady(Playlist likedPlaylist) {
                boolean isLiked = isSongLiked(likedPlaylist, song);
                
                if (isLiked) {
                    // Șterge din liked songs
                    removeSongFromLiked(likedPlaylist, song, callback);
                } else {
                    // Adaugă în liked songs
                    addSongToLiked(likedPlaylist, song, callback);
                }
            }
            
            @Override
            public void onError(String error) {
                callback.onError(error);
            }
        });
    }
    
    /**
     * Verifică dacă o melodie este deja în "Liked Songs"
     */
    public static void checkIfLiked(String userId, Song song, OnLikeStatusChecked callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        
        db.collection("playlists")
            .whereEqualTo("userId", userId)
            .whereEqualTo("name", LIKED_SONGS_PLAYLIST_NAME)
            .limit(1)
            .get()
            .addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult() != null && !task.getResult().isEmpty()) {
                    QueryDocumentSnapshot doc = (QueryDocumentSnapshot) task.getResult().getDocuments().get(0);
                    
                    @SuppressWarnings("unchecked")
                    List<String> songIds = (List<String>) doc.get("songIds");
                    @SuppressWarnings("unchecked")
                    List<String> cloudSongIds = (List<String>) doc.get("cloudSongIds");
                    
                    boolean isLiked = false;
                    if (song.isCloudSong()) {
                        // Pentru melodiile cloud, verifică cloudSongIds
                        String songIdentifier = getCloudSongIdentifier(song);
                        if (cloudSongIds != null && songIdentifier != null) {
                            // Trebuie să obținem ID-ul din Firestore pentru melodia cloud
                            checkCloudSongInLiked(userId, song, cloudSongIds, callback);
                            return;
                        }
                    } else {
                        // Pentru melodiile locale, verifică songIds
                        if (songIds != null && songIds.contains(String.valueOf(song.getResId()))) {
                            isLiked = true;
                        }
                    }
                    callback.onChecked(isLiked);
                } else {
                    callback.onChecked(false);
                }
            });
    }
    
    private static void checkCloudSongInLiked(String userId, Song song, List<String> cloudSongIds, OnLikeStatusChecked callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        
        // Caută melodia cloud în Firestore
        db.collection("cloud_songs")
            .whereEqualTo("userId", userId)
            .whereEqualTo("cloudUrl", song.getCloudUrl())
            .limit(1)
            .get()
            .addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult() != null && !task.getResult().isEmpty()) {
                    String cloudSongId = task.getResult().getDocuments().get(0).getId();
                    callback.onChecked(cloudSongIds != null && cloudSongIds.contains(cloudSongId));
                } else {
                    callback.onChecked(false);
                }
            });
    }
    
    private static boolean isSongLiked(Playlist likedPlaylist, Song song) {
        if (song.isCloudSong()) {
            // Pentru melodiile cloud, verificăm cloudSongIds
            // Trebuie să obținem ID-ul din Firestore
            return false; // Va fi verificat în checkIfLiked
        } else {
            // Pentru melodiile locale
            return likedPlaylist.getSongIds().contains(String.valueOf(song.getResId()));
        }
    }
    
    private static void addSongToLiked(Playlist likedPlaylist, Song song, OnLikeToggled callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        
        if (song.isCloudSong()) {
            // Pentru melodiile cloud, obținem ID-ul din Firestore
            String userId = FirebaseAuth.getInstance().getCurrentUser() != null ? 
                FirebaseAuth.getInstance().getCurrentUser().getUid() : null;
            
            if (userId == null) {
                callback.onError("User not logged in");
                return;
            }
            
            db.collection("cloud_songs")
                .whereEqualTo("userId", userId)
                .whereEqualTo("cloudUrl", song.getCloudUrl())
                .limit(1)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null && !task.getResult().isEmpty()) {
                        String cloudSongId = task.getResult().getDocuments().get(0).getId();
                        List<String> cloudSongIds = new ArrayList<>(likedPlaylist.getCloudSongIds());
                        
                        if (!cloudSongIds.contains(cloudSongId)) {
                            cloudSongIds.add(cloudSongId);
                            
                            db.collection("playlists").document(likedPlaylist.getId())
                                .update("cloudSongIds", cloudSongIds)
                                .addOnCompleteListener(updateTask -> {
                                    if (updateTask.isSuccessful()) {
                                        callback.onToggled(true, "Added to Liked Songs");
                                    } else {
                                        callback.onError("Failed to add to Liked Songs");
                                    }
                                });
                        } else {
                            callback.onToggled(true, "Already in Liked Songs");
                        }
                    } else {
                        callback.onError("Cloud song not found");
                    }
                });
        } else {
            // Pentru melodiile locale
            List<String> songIds = new ArrayList<>(likedPlaylist.getSongIds());
            String songId = String.valueOf(song.getResId());
            
            if (!songIds.contains(songId)) {
                songIds.add(songId);
                
                db.collection("playlists").document(likedPlaylist.getId())
                    .update("songIds", songIds)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            callback.onToggled(true, "Added to Liked Songs");
                        } else {
                            callback.onError("Failed to add to Liked Songs");
                        }
                    });
            } else {
                callback.onToggled(true, "Already in Liked Songs");
            }
        }
    }
    
    private static void removeSongFromLiked(Playlist likedPlaylist, Song song, OnLikeToggled callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        
        if (song.isCloudSong()) {
            // Pentru melodiile cloud
            String userId = FirebaseAuth.getInstance().getCurrentUser() != null ? 
                FirebaseAuth.getInstance().getCurrentUser().getUid() : null;
            
            if (userId == null) {
                callback.onError("User not logged in");
                return;
            }
            
            db.collection("cloud_songs")
                .whereEqualTo("userId", userId)
                .whereEqualTo("cloudUrl", song.getCloudUrl())
                .limit(1)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null && !task.getResult().isEmpty()) {
                        String cloudSongId = task.getResult().getDocuments().get(0).getId();
                        List<String> cloudSongIds = new ArrayList<>(likedPlaylist.getCloudSongIds());
                        cloudSongIds.remove(cloudSongId);
                        
                        db.collection("playlists").document(likedPlaylist.getId())
                            .update("cloudSongIds", cloudSongIds)
                            .addOnCompleteListener(updateTask -> {
                                if (updateTask.isSuccessful()) {
                                    callback.onToggled(false, "Removed from Liked Songs");
                                } else {
                                    callback.onError("Failed to remove from Liked Songs");
                                }
                            });
                    } else {
                        callback.onError("Cloud song not found");
                    }
                });
        } else {
            // Pentru melodiile locale
            List<String> songIds = new ArrayList<>(likedPlaylist.getSongIds());
            songIds.remove(String.valueOf(song.getResId()));
            
            db.collection("playlists").document(likedPlaylist.getId())
                .update("songIds", songIds)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        callback.onToggled(false, "Removed from Liked Songs");
                    } else {
                        callback.onError("Failed to remove from Liked Songs");
                    }
                });
        }
    }
    
    private static String getCloudSongIdentifier(Song song) {
        // Returnează un identificator pentru melodia cloud
        return song.getCloudUrl();
    }
    
    public interface OnLikedPlaylistReady {
        void onReady(Playlist playlist);
        void onError(String error);
    }
    
    public interface OnLikeToggled {
        void onToggled(boolean isLiked, String message);
        void onError(String error);
    }
    
    public interface OnLikeStatusChecked {
        void onChecked(boolean isLiked);
    }
}
