package com.example.musicplayer;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Extend BaseActivity
public class GenreActivity extends BaseActivity {

    final String[] GENRE_NAMES = {
            "Hip-Hop", "Rock", "Alternative", "Romanian Rock",
            "Romanian Alternative", "Romanian Hip-Hop"
    };

    final String[] GENRE_IMAGES = {
            "genre_hiphop", "genre_rock", "genre_alternative",
            "genre_ro_rock", "genre_ro_alternative", "genre_ro_hiphop"
    };
    
    private GridView gridView;
    private ListView listViewPlaylists;
    private List<Playlist> userPlaylists;
    private PlaylistAdapter playlistAdapter;
    private FirebaseFirestore db;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_genres);

        db = FirebaseFirestore.getInstance();
        sessionManager = new SessionManager(this);
        userPlaylists = new ArrayList<>();

        gridView = findViewById(R.id.gvGenres);
        listViewPlaylists = findViewById(R.id.lvUserPlaylists);
        
        // Setup GridView pentru genuri
        GenreAdapter genreAdapter = new GenreAdapter();
        gridView.setAdapter(genreAdapter);
        gridView.setOnItemClickListener((parent, view, position, id) -> {
            String selectedGenre = GENRE_NAMES[position];
            String selectedImage = GENRE_IMAGES[position];
            
            Intent intent = new Intent(GenreActivity.this, LibraryActivity.class);
            intent.putExtra("GENRE_FILTER", selectedGenre);
            intent.putExtra("GENRE_IMAGE", selectedImage);
            startActivity(intent);
        });
        
        // Setup ListView pentru playlist-urile utilizatorului
        TextView tvPlaylistsTitle = findViewById(R.id.tvPlaylistsTitle);
        if (tvPlaylistsTitle != null) {
            tvPlaylistsTitle.setText("My Playlists");
        }
        
        playlistAdapter = new PlaylistAdapter(userPlaylists);
        listViewPlaylists.setAdapter(playlistAdapter);
        loadUserPlaylists();
        
        listViewPlaylists.setOnItemClickListener((parent, view, position, id) -> {
            Playlist playlist = userPlaylists.get(position);
            showPlaylistOptionsDialog(playlist);
        });

        setupMiniPlayer();
        setupBottomNavigation();
    }
    
    private void loadUserPlaylists() {
        String userId = sessionManager.getUserId();
        if (userId == null) return;

        db.collection("playlists")
            .whereEqualTo("userId", userId)
            .get()
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    userPlaylists.clear();
                    boolean hasLikedSongs = false;
                    
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        Playlist playlist = new Playlist();
                        playlist.setId(document.getId());
                        playlist.setName(document.getString("name"));
                        playlist.setUserId(document.getString("userId"));
                        
                        // Verifică dacă există deja "Liked Songs"
                        if ("Liked Songs".equals(playlist.getName())) {
                            hasLikedSongs = true;
                        }
                        
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
                        
                        userPlaylists.add(playlist);
                    }
                    
                    // Dacă nu există "Liked Songs", îl creează automat
                    if (!hasLikedSongs) {
                        createLikedSongsPlaylistIfNeeded(userId);
                    }
                    
                    playlistAdapter.notifyDataSetChanged();
                }
            });
    }
    
    private void createLikedSongsPlaylistIfNeeded(String userId) {
        Map<String, Object> playlist = new HashMap<>();
        playlist.put("name", "Liked Songs");
        playlist.put("userId", userId);
        playlist.put("songIds", new ArrayList<String>());
        playlist.put("cloudSongIds", new ArrayList<String>());
        playlist.put("createdAt", System.currentTimeMillis());
        
        db.collection("playlists")
            .add(playlist)
            .addOnSuccessListener(documentReference -> {
                // Reîncarcă playlist-urile după creare
                loadUserPlaylists();
            });
    }
    
    private void showPlaylistOptionsDialog(Playlist playlist) {
        String[] options = {"Play Playlist", "View Songs"};
        
        new AlertDialog.Builder(this)
            .setTitle(playlist.getName())
            .setItems(options, (dialog, which) -> {
                switch (which) {
                    case 0: // Play Playlist
                        playPlaylist(playlist);
                        break;
                    case 1: // View Songs
                        viewPlaylistSongs(playlist);
                        break;
                }
            })
            .setNegativeButton("Cancel", null)
            .show();
    }
    
    private void playPlaylist(Playlist playlist) {
        if (playlist.getSongCount() == 0) {
            Toast.makeText(this, "Playlist is empty. Add some songs first!", Toast.LENGTH_SHORT).show();
            return;
        }
        
        PlaylistHelper.getPlaylistSongs(this, playlist, new PlaylistHelper.OnPlaylistSongsLoaded() {
            @Override
            public void onSuccess(List<Song> songs) {
                if (songs.isEmpty()) {
                    Toast.makeText(GenreActivity.this, "No songs found in playlist", Toast.LENGTH_SHORT).show();
                } else {
                    // Redă playlist-ul
                    MusicPlayerManager.getInstance().playSong(GenreActivity.this, songs, 0);
                    Toast.makeText(GenreActivity.this, "Playing: " + playlist.getName(), Toast.LENGTH_SHORT).show();
                    // Deschide player-ul
                    startActivity(new Intent(GenreActivity.this, MainActivity.class));
                }
            }
            
            @Override
            public void onError(String error) {
                Toast.makeText(GenreActivity.this, error, Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void viewPlaylistSongs(Playlist playlist) {
        PlaylistHelper.getPlaylistSongs(this, playlist, new PlaylistHelper.OnPlaylistSongsLoaded() {
            @Override
            public void onSuccess(List<Song> songs) {
                if (songs.isEmpty()) {
                    Toast.makeText(GenreActivity.this, "Playlist is empty", Toast.LENGTH_SHORT).show();
                } else {
                    // Deschide LibraryActivity cu melodiile din playlist
                    Intent intent = new Intent(GenreActivity.this, LibraryActivity.class);
                    intent.putExtra("PLAYLIST_NAME", playlist.getName());
                    intent.putExtra("PLAYLIST_SONGS", new ArrayList<>(songs));
                    startActivity(intent);
                }
            }
            
            @Override
            public void onError(String error) {
                Toast.makeText(GenreActivity.this, error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private class GenreAdapter extends BaseAdapter {
        @Override
        public int getCount() { return GENRE_NAMES.length; }
        @Override
        public Object getItem(int position) { return GENRE_NAMES[position]; }
        @Override
        public long getItemId(int position) { return position; }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(GenreActivity.this)
                        .inflate(R.layout.item_genre, parent, false);
            }

            TextView tvName = convertView.findViewById(R.id.tvGenreName);
            ImageView ivImage = convertView.findViewById(R.id.ivGenreImage);

            tvName.setText(GENRE_NAMES[position]);

            int resId = getResources().getIdentifier(
                    GENRE_IMAGES[position], "drawable", getPackageName());

            if (resId != 0) {
                ivImage.setImageResource(resId);
            } else {
                ivImage.setImageResource(android.R.drawable.ic_menu_gallery);
            }

            return convertView;
        }
    }
    
    private class PlaylistAdapter extends BaseAdapter {
        private List<Playlist> playlists;

        public PlaylistAdapter(List<Playlist> playlists) {
            this.playlists = playlists;
        }

        @Override
        public int getCount() {
            return playlists.size();
        }

        @Override
        public Object getItem(int position) {
            return playlists.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(GenreActivity.this)
                        .inflate(R.layout.item_playlist, parent, false);
            }

            Playlist playlist = playlists.get(position);
            TextView tvName = convertView.findViewById(R.id.tvPlaylistName);
            TextView tvCount = convertView.findViewById(R.id.tvPlaylistSongCount);
            ImageButton btnDelete = convertView.findViewById(R.id.btnDeletePlaylist);
            ImageView ivPlaylistImage = convertView.findViewById(R.id.ivPlaylistImage);

            tvName.setText(playlist.getName());
            tvCount.setText(playlist.getSongCount() + " songs");

            // Setează imaginea default pentru "Liked Songs"
            if ("Liked Songs".equals(playlist.getName())) {
                ivPlaylistImage.setImageResource(R.drawable.ic_liked_songs);
                ivPlaylistImage.setBackgroundTintList(null); // Elimină background tint pentru heart
            } else {
                ivPlaylistImage.setImageResource(R.drawable.ic_playlist);
                ivPlaylistImage.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#1DB954")));
            }

            // Ascunde butonul de delete în GenreActivity (doar pentru redare)
            btnDelete.setVisibility(View.GONE);

            return convertView;
        }
    }
}