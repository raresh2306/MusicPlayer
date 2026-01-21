package com.example.musicplayer;

import android.content.Context;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LikedSongsHelper {
    private static final String LIKED_SONGS_PLAYLIST_NAME = "Liked Songs";

    public static void getOrCreateLikedSongsPlaylist(String userId, OnLikedPlaylistReady callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("playlists")
                .whereEqualTo("userId", userId)
                .whereEqualTo("name", LIKED_SONGS_PLAYLIST_NAME)
                .limit(1)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null && !task.getResult().isEmpty()) {
                        QueryDocumentSnapshot doc = (QueryDocumentSnapshot) task.getResult().getDocuments().get(0);
                        Playlist playlist = new Playlist();
                        playlist.setId(doc.getId());
                        playlist.setName(doc.getString("name"));
                        playlist.setUserId(doc.getString("userId"));

                        @SuppressWarnings("unchecked")
                        List<String> songIds = (List<String>) doc.get("songIds");
                        if (songIds != null) playlist.setSongIds(songIds);

                        @SuppressWarnings("unchecked")
                        List<String> cloudSongIds = (List<String>) doc.get("cloudSongIds");
                        if (cloudSongIds != null) playlist.setCloudSongIds(cloudSongIds);

                        callback.onReady(playlist);
                    } else {
                        createLikedSongsPlaylist(userId, callback);
                    }
                });
    }

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
                .addOnFailureListener(e -> callback.onError("Failed to create Liked Songs playlist"));
    }

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
                if (isSongLiked(likedPlaylist, song)) {
                    removeSongFromLiked(likedPlaylist, song, callback);
                } else {
                    addSongToLiked(likedPlaylist, song, callback);
                }
            }
            @Override
            public void onError(String error) {
                callback.onError(error);
            }
        });
    }

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
                            checkCloudSongInLiked(userId, song, cloudSongIds, callback);
                            return;
                        } else {
                            String stableId = song.getStringId();
                            String oldResId = String.valueOf(song.getResId());
                            if (songIds != null && (songIds.contains(stableId) || songIds.contains(oldResId))) {
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
        if (song.isCloudSong()) return false;
        return likedPlaylist.getSongIds().contains(song.getStringId()) ||
                likedPlaylist.getSongIds().contains(String.valueOf(song.getResId()));
    }

    private static void addSongToLiked(Playlist likedPlaylist, Song song, OnLikeToggled callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        if (song.isCloudSong()) {
            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
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
                                        .addOnSuccessListener(v -> callback.onToggled(true, "Added to Liked Songs"))
                                        .addOnFailureListener(e -> callback.onError("Failed to add"));
                            } else {
                                callback.onToggled(true, "Already in Liked Songs");
                            }
                        } else {
                            callback.onError("Cloud song not found");
                        }
                    });
        } else {
            List<String> songIds = new ArrayList<>(likedPlaylist.getSongIds());
            String stableId = song.getStringId();

            if (!songIds.contains(stableId)) {
                songIds.add(stableId);
                db.collection("playlists").document(likedPlaylist.getId())
                        .update("songIds", songIds)
                        .addOnSuccessListener(v -> callback.onToggled(true, "Added to Liked Songs"))
                        .addOnFailureListener(e -> callback.onError("Failed to add"));
            } else {
                callback.onToggled(true, "Already in Liked Songs");
            }
        }
    }

    private static void removeSongFromLiked(Playlist likedPlaylist, Song song, OnLikeToggled callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        if (song.isCloudSong()) {
            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
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
                                    .addOnSuccessListener(v -> callback.onToggled(false, "Removed from Liked Songs"));
                        }
                    });
        } else {
            List<String> songIds = new ArrayList<>(likedPlaylist.getSongIds());
            songIds.remove(song.getStringId());
            songIds.remove(String.valueOf(song.getResId()));

            db.collection("playlists").document(likedPlaylist.getId())
                    .update("songIds", songIds)
                    .addOnSuccessListener(v -> callback.onToggled(false, "Removed from Liked Songs"))
                    .addOnFailureListener(e -> callback.onError("Failed to remove"));
        }
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