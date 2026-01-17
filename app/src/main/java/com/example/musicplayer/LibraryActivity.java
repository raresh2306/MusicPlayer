package com.example.musicplayer;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class LibraryActivity extends BaseActivity {

    private List<Song> displayedSongs;
    private boolean isAddToPlaylistMode = false;
    private String targetPlaylistId = null;
    private String targetPlaylistName = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_library);

        ListView listView = findViewById(R.id.lvAllSongs);
        TextView title = findViewById(R.id.tvLibraryTitle);
        ImageView playlistImage = findViewById(R.id.ivLibraryImage);

        FloatingActionButton btnPlay = findViewById(R.id.btnPlayPlaylist);

        String genreFilter = getIntent().getStringExtra("GENRE_FILTER");
        String genreImageName = getIntent().getStringExtra("GENRE_IMAGE");
        String playlistName = getIntent().getStringExtra("PLAYLIST_NAME");
        String playlistId = getIntent().getStringExtra("PLAYLIST_ID");
        isAddToPlaylistMode = getIntent().getBooleanExtra("ADD_TO_PLAYLIST_MODE", false);
        targetPlaylistId = playlistId;
        targetPlaylistName = playlistName;
        @SuppressWarnings("unchecked")
        ArrayList<Song> playlistSongs = (ArrayList<Song>) getIntent().getSerializableExtra("PLAYLIST_SONGS");

        // 1. Setup Data and UI
        if (isAddToPlaylistMode) {
            // Case E: Add to Playlist Mode - show all songs from library
            displayedSongs = MusicLibrary.getSongList(this);
            title.setText("Add songs to: " + (playlistName != null ? playlistName : "Playlist"));
            int resId = getResources().getIdentifier("library_cover", "drawable", getPackageName());
            if (resId != 0) {
                playlistImage.setImageResource(resId);
            }
        } else if (playlistId != null) {
            // Case D: Opened from Playlist with ID (from GenreActivity or ProfileActivity)
            Playlist tempPlaylist = new Playlist();
            tempPlaylist.setId(playlistId);
            tempPlaylist.setName(playlistName != null ? playlistName : "Playlist");
            
            PlaylistHelper.getPlaylistSongs(this, tempPlaylist, new PlaylistHelper.OnPlaylistSongsLoaded() {
                @Override
                public void onSuccess(List<Song> songs) {
                    displayedSongs = songs;
                    if (displayedSongs == null) {
                        displayedSongs = new ArrayList<>();
                    }
                    title.setText(playlistName != null ? playlistName : "Playlist");
                    int resId = getResources().getIdentifier("library_cover", "drawable", getPackageName());
                    if (resId != 0) {
                        playlistImage.setImageResource(resId);
                    }
                    LibraryAdapter adapter = new LibraryAdapter(displayedSongs);
                    listView.setAdapter(adapter);
                }

                @Override
                public void onError(String error) {
                    displayedSongs = new ArrayList<>();
                    title.setText(playlistName != null ? playlistName : "Playlist");
                    LibraryAdapter adapter = new LibraryAdapter(displayedSongs);
                    listView.setAdapter(adapter);
                }
            });
        } else if (playlistSongs != null && !playlistSongs.isEmpty()) {
            // Case C: Opened from Playlist
            displayedSongs = playlistSongs;
            title.setText(playlistName != null ? playlistName : "Playlist");
            int resId = getResources().getIdentifier("library_cover", "drawable", getPackageName());
            if (resId != 0) {
                playlistImage.setImageResource(resId);
            }
        } else if (genreFilter != null) {
            // Case A: Opened from Genres Page (Filtered)
            displayedSongs = MusicLibrary.getSongsByGenre(this, genreFilter);
            title.setText(genreFilter);

            if (genreImageName != null) {
                int resId = getResources().getIdentifier(genreImageName, "drawable", getPackageName());
                if (resId != 0) playlistImage.setImageResource(resId);
            }
        } else {
            // Case B: Opened "Music Library" (All Songs)
            displayedSongs = MusicLibrary.getSongList(this);
            title.setText("All Music");

            // NEW: Set the specific image for the main library
            int resId = getResources().getIdentifier("library_cover", "drawable", getPackageName());
            if (resId != 0) {
                playlistImage.setImageResource(resId);
            }
        }

        // Ensure displayedSongs is not null
        if (displayedSongs == null) {
            displayedSongs = new ArrayList<>();
        }

        LibraryAdapter adapter = new LibraryAdapter(displayedSongs);
        listView.setAdapter(adapter);

        // 2. Play Button Logic
        btnPlay.setOnClickListener(v -> {
            if (displayedSongs != null && !displayedSongs.isEmpty()) {
                if (isAddToPlaylistMode) {
                    // In add mode, show dialog to select songs
                    showAddSongsToPlaylistDialog();
                } else {
                    MusicPlayerManager.getInstance().playSong(LibraryActivity.this, displayedSongs, 0);
                }
            }
        });

        // 3. List Click Logic - Just play the song without opening MainActivity
        listView.setOnItemClickListener((parent, view, position, id) -> {
            if (displayedSongs != null && position < displayedSongs.size()) {
                if (isAddToPlaylistMode) {
                    // In add mode, show dialog to select songs
                    showAddSongsToPlaylistDialog();
                } else {
                    // Just play the song
                    MusicPlayerManager.getInstance().playSong(LibraryActivity.this, displayedSongs, position);
                }
            }
        });

        // 4. Long-click to add song to playlist
        listView.setOnItemLongClickListener((parent, view, position, id) -> {
            if (displayedSongs != null && position < displayedSongs.size() && !isAddToPlaylistMode) {
                Song song = displayedSongs.get(position);
                showAddToPlaylistDialog(song);
                return true;
            }
            return false;
        });

        setupMiniPlayer();
        setupBottomNavigation();
    }

    private class LibraryAdapter extends BaseAdapter {
        private List<Song> songs;

        public LibraryAdapter(List<Song> songs) {
            this.songs = songs;
        }

        @Override
        public int getCount() { return songs.size(); }
        @Override
        public Object getItem(int position) { return songs.get(position); }
        @Override
        public long getItemId(int position) { return position; }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(LibraryActivity.this)
                        .inflate(R.layout.item_library_song, parent, false);
            }
            Song song = songs.get(position);

            ImageView ivImage = convertView.findViewById(R.id.ivLibArtistImage);
            TextView tvTitle = convertView.findViewById(R.id.tvLibSongTitle);
            TextView tvArtist = convertView.findViewById(R.id.tvLibArtist);

            tvTitle.setText(song.getTitle());
            tvArtist.setText(song.getArtist());

            int artistImageRes = ArtistImageHelper.getArtistImageResource(
                    LibraryActivity.this, song.getArtist());
            ivImage.setImageResource(artistImageRes);

            return convertView;
        }
    }

    private void showAddToPlaylistDialog(Song song) {
        String userId = FirebaseAuth.getInstance().getCurrentUser() != null ? 
            FirebaseAuth.getInstance().getCurrentUser().getUid() : null;
        
        if (userId == null) {
            Toast.makeText(this, "Please login to add songs to playlists", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("playlists")
            .whereEqualTo("userId", userId)
            .get()
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    List<Playlist> playlists = new ArrayList<>();
                    List<String> playlistNames = new ArrayList<>();
                    
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        Playlist playlist = new Playlist();
                        playlist.setId(document.getId());
                        playlist.setName(document.getString("name"));
                        playlist.setUserId(document.getString("userId"));
                        
                        @SuppressWarnings("unchecked")
                        List<String> songIds = (List<String>) document.get("songIds");
                        if (songIds != null) {
                            playlist.setSongIds(songIds);
                        }
                        
                        @SuppressWarnings("unchecked")
                        List<String> cloudSongIds = (List<String>) document.get("cloudSongIds");
                        if (cloudSongIds != null) {
                            playlist.setCloudSongIds(cloudSongIds);
                        }
                        
                        playlists.add(playlist);
                        playlistNames.add(playlist.getName());
                    }
                    
                    if (playlists.isEmpty()) {
                        Toast.makeText(this, "No playlists found. Create one first!", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    
                    String[] playlistArray = playlistNames.toArray(new String[0]);
                    
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Add to Playlist")
                        .setItems(playlistArray, (dialog, which) -> {
                            Playlist selectedPlaylist = playlists.get(which);
                            addSongToPlaylist(selectedPlaylist, song);
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
                } else {
                    Toast.makeText(this, "Failed to load playlists", Toast.LENGTH_SHORT).show();
                }
            });
    }

    private void addSongToPlaylist(Playlist playlist, Song song) {
        if (playlist.getId() == null) return;
        
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        
        if (song.isCloudSong()) {
            // Pentru melodiile cloud
            String userId = FirebaseAuth.getInstance().getCurrentUser() != null ? 
                FirebaseAuth.getInstance().getCurrentUser().getUid() : null;
            
            if (userId == null) {
                Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
                return;
            }
            
            // Obține ID-ul melodiei cloud din Firestore
            db.collection("cloud_songs")
                .whereEqualTo("userId", userId)
                .whereEqualTo("cloudUrl", song.getCloudUrl())
                .limit(1)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null && !task.getResult().isEmpty()) {
                        String cloudSongId = task.getResult().getDocuments().get(0).getId();
                        List<String> cloudSongIds = new ArrayList<>(playlist.getCloudSongIds());
                        
                        if (cloudSongIds.contains(cloudSongId)) {
                            Toast.makeText(LibraryActivity.this, "Song already in playlist", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        
                        cloudSongIds.add(cloudSongId);
                        
                        db.collection("playlists").document(playlist.getId())
                            .update("cloudSongIds", cloudSongIds)
                            .addOnCompleteListener(updateTask -> {
                                if (updateTask.isSuccessful()) {
                                    Toast.makeText(LibraryActivity.this, "Added to " + playlist.getName(), Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(LibraryActivity.this, "Failed to add song to playlist", Toast.LENGTH_SHORT).show();
                                }
                            });
                    } else {
                        Toast.makeText(LibraryActivity.this, "Cloud song not found", Toast.LENGTH_SHORT).show();
                    }
                });
        } else {
            // Pentru melodiile locale
            String songId = String.valueOf(song.getResId());
            List<String> songIds = new ArrayList<>(playlist.getSongIds());
            
            // Verifică dacă melodia nu este deja în playlist
            if (songIds.contains(songId)) {
                Toast.makeText(this, "Song already in playlist", Toast.LENGTH_SHORT).show();
                return;
            }
            
            songIds.add(songId);
            
            db.collection("playlists").document(playlist.getId())
                .update("songIds", songIds)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Added to " + playlist.getName(), Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Failed to add song to playlist", Toast.LENGTH_SHORT).show();
                    }
                });
        }
    }

    private void showAddSongsToPlaylistDialog() {
        if (targetPlaylistId == null || displayedSongs == null || displayedSongs.isEmpty()) {
            Toast.makeText(this, "No songs available", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get current playlist to check which songs are already in it
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("playlists").document(targetPlaylistId)
            .get()
            .addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult() != null) {
                    Playlist playlist = new Playlist();
                    playlist.setId(targetPlaylistId);
                    playlist.setName(targetPlaylistName);
                    
                    @SuppressWarnings("unchecked")
                    List<String> songIds = (List<String>) task.getResult().get("songIds");
                    if (songIds != null) {
                        playlist.setSongIds(songIds);
                    }
                    
                    @SuppressWarnings("unchecked")
                    List<String> cloudSongIds = (List<String>) task.getResult().get("cloudSongIds");
                    if (cloudSongIds != null) {
                        playlist.setCloudSongIds(cloudSongIds);
                    }

                    // Create list of song names and checked state
                    String[] songNames = new String[displayedSongs.size()];
                    boolean[] checkedSongs = new boolean[displayedSongs.size()];
                    
                    for (int i = 0; i < displayedSongs.size(); i++) {
                        Song song = displayedSongs.get(i);
                        songNames[i] = song.getTitle() + " - " + song.getArtist();
                        
                        // Check if song is already in playlist
                        if (song.isCloudSong()) {
                            // For cloud songs, we need to check by cloudUrl
                            String userId = FirebaseAuth.getInstance().getCurrentUser() != null ? 
                                FirebaseAuth.getInstance().getCurrentUser().getUid() : null;
                            if (userId != null) {
                                // We'll check this after loading cloud songs
                                checkedSongs[i] = false;
                            }
                        } else {
                            checkedSongs[i] = playlist.getSongIds().contains(String.valueOf(song.getResId()));
                        }
                    }
                    
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Add songs to: " + (targetPlaylistName != null ? targetPlaylistName : "Playlist"))
                        .setMultiChoiceItems(songNames, checkedSongs, (dialog, which, isChecked) -> {
                            checkedSongs[which] = isChecked;
                        })
                        .setPositiveButton("Add Selected", (dialog, which) -> {
                            // Add selected songs to playlist
                            List<Song> songsToAdd = new ArrayList<>();
                            for (int i = 0; i < checkedSongs.length; i++) {
                                if (checkedSongs[i]) {
                                    songsToAdd.add(displayedSongs.get(i));
                                }
                            }
                            
                            if (songsToAdd.isEmpty()) {
                                Toast.makeText(this, "No songs selected", Toast.LENGTH_SHORT).show();
                                return;
                            }
                            
                            addSongsToPlaylist(playlist, songsToAdd);
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
                } else {
                    Toast.makeText(this, "Failed to load playlist", Toast.LENGTH_SHORT).show();
                }
            });
    }

    private void addSongsToPlaylist(Playlist playlist, List<Song> songs) {
        if (playlist.getId() == null) return;
        
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        List<String> songIds = new ArrayList<>(playlist.getSongIds());
        List<String> cloudSongIds = new ArrayList<>(playlist.getCloudSongIds());
        String userId = FirebaseAuth.getInstance().getCurrentUser() != null ? 
            FirebaseAuth.getInstance().getCurrentUser().getUid() : null;
        
        // Separate local and cloud songs
        List<Song> localSongs = new ArrayList<>();
        List<Song> cloudSongs = new ArrayList<>();
        
        for (Song song : songs) {
            if (song.isCloudSong()) {
                cloudSongs.add(song);
            } else {
                localSongs.add(song);
            }
        }
        
        // Add local songs
        final int[] addedCount = {0};
        final int[] alreadyInCount = {0};
        for (Song song : localSongs) {
            String songId = String.valueOf(song.getResId());
            if (!songIds.contains(songId)) {
                songIds.add(songId);
                addedCount[0]++;
            } else {
                alreadyInCount[0]++;
            }
        }
        
        // Handle cloud songs if any
        if (!cloudSongs.isEmpty() && userId != null) {
            // Get all cloud song IDs
            db.collection("cloud_songs")
                .whereEqualTo("userId", userId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot doc : task.getResult()) {
                            String cloudSongId = doc.getId();
                            String cloudUrl = doc.getString("cloudUrl");
                            
                            // Check if this cloud song is in our list to add
                            for (Song song : cloudSongs) {
                                if (song.getCloudUrl().equals(cloudUrl) && !cloudSongIds.contains(cloudSongId)) {
                                    cloudSongIds.add(cloudSongId);
                                    addedCount[0]++;
                                    break;
                                }
                            }
                        }
                        
                        // Update playlist with all songs
                        updatePlaylistWithSongs(playlist, songIds, cloudSongIds, addedCount[0], alreadyInCount[0]);
                    } else {
                        // Update with just local songs if cloud fetch fails
                        updatePlaylistWithSongs(playlist, songIds, cloudSongIds, addedCount[0], alreadyInCount[0]);
                    }
                });
        } else {
            // No cloud songs, just update with local songs
            updatePlaylistWithSongs(playlist, songIds, cloudSongIds, addedCount[0], alreadyInCount[0]);
        }
    }
    
    private void updatePlaylistWithSongs(Playlist playlist, List<String> songIds, List<String> cloudSongIds, 
                                        int addedCount, int alreadyInCount) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("playlists").document(playlist.getId())
            .update("songIds", songIds, "cloudSongIds", cloudSongIds)
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    String message = "Added " + addedCount + " song(s)";
                    if (alreadyInCount > 0) {
                        message += " (" + alreadyInCount + " already in playlist)";
                    }
                    Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                    // Close this activity and return to playlist
                    finish();
                } else {
                    Toast.makeText(this, "Failed to add songs to playlist", Toast.LENGTH_SHORT).show();
                }
            });
    }
}