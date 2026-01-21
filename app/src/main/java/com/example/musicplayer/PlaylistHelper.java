package com.example.musicplayer;

import android.content.Context;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class PlaylistHelper {

    public static void getPlaylistSongs(Context context, Playlist playlist, OnPlaylistSongsLoaded callback) {
        List<Song> playlistSongs = new ArrayList<>();
        List<Song> allSongs = MusicLibrary.getSongList(context);

        for (String savedId : playlist.getSongIds()) {
            boolean found = false;
            for (Song song : allSongs) {
                if (!song.isCloudSong()) {
                    if (savedId.equals(song.getStringId())) {
                        playlistSongs.add(song);
                        found = true;
                        break;
                    }
                }
            }
            if (!found) {
                try {
                    int oldResId = Integer.parseInt(savedId);
                    for (Song song : allSongs) {
                        if (!song.isCloudSong() && song.getResId() == oldResId) {
                            playlistSongs.add(song);
                            break;
                        }
                    }
                } catch (NumberFormatException e) { }
            }
        }

        if (!playlist.getCloudSongIds().isEmpty()) {
            loadCloudSongsFromPlaylist(context, playlist.getCloudSongIds(), playlistSongs, callback);
        } else {
            callback.onSuccess(playlistSongs);
        }
    }

    private static void loadCloudSongsFromPlaylist(Context context, List<String> cloudSongIds,
                                                   List<Song> playlistSongs, OnPlaylistSongsLoaded callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String userId = FirebaseAuth.getInstance().getCurrentUser() != null ?
                FirebaseAuth.getInstance().getCurrentUser().getUid() : null;

        if (userId == null) {
            callback.onSuccess(playlistSongs);
            return;
        }

        db.collection("cloud_songs")
                .whereEqualTo("userId", userId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot doc : task.getResult()) {
                            String songId = doc.getId();
                            if (cloudSongIds.contains(songId)) {
                                String cloudUrl = doc.getString("cloudUrl");
                                String title = doc.getString("title");
                                String artist = doc.getString("artist");
                                String genre = doc.getString("genre");
                                String coverImageUrl = doc.getString("coverImageUrl");

                                if (cloudUrl != null && title != null) {
                                    Song cloudSong;
                                    if (coverImageUrl != null && !coverImageUrl.isEmpty()) {
                                        cloudSong = new Song(cloudUrl, title,
                                                artist != null ? artist : "Unknown",
                                                genre != null ? genre : "Unknown",
                                                coverImageUrl);
                                    } else {
                                        cloudSong = new Song(cloudUrl, title,
                                                artist != null ? artist : "Unknown",
                                                genre != null ? genre : "Unknown");
                                    }
                                    playlistSongs.add(cloudSong);
                                }
                            }
                        }
                        callback.onSuccess(playlistSongs);
                    } else {
                        callback.onError("Failed to load cloud songs from playlist");
                    }
                });
    }

    public interface OnPlaylistSongsLoaded {
        void onSuccess(List<Song> songs);
        void onError(String error);
    }
}