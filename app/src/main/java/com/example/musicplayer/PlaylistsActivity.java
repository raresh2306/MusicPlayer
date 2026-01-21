package com.example.musicplayer;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import com.google.android.material.textfield.TextInputEditText;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlaylistsActivity extends BaseActivity {

    private ListView listViewPlaylists;
    private List<Playlist> userPlaylists;
    private PlaylistAdapter playlistAdapter;
    private FirebaseFirestore db;
    private SessionManager sessionManager;
    private Button btnCreatePlaylist;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playlists);

        db = FirebaseFirestore.getInstance();
        sessionManager = new SessionManager(this);
        userPlaylists = new ArrayList<>();

        listViewPlaylists = findViewById(R.id.lvUserPlaylists);

        TextView tvPlaylistsTitle = findViewById(R.id.tvPlaylistsTitle);
        if (tvPlaylistsTitle != null) {
            tvPlaylistsTitle.setText("My Playlists");
        }

        btnCreatePlaylist = findViewById(R.id.btnCreatePlaylist);
        if (btnCreatePlaylist != null) {
            btnCreatePlaylist.setOnClickListener(v -> showCreatePlaylistDialog());
        }

        playlistAdapter = new PlaylistAdapter(userPlaylists);
        listViewPlaylists.setAdapter(playlistAdapter);

        loadUserPlaylists();
        setupMiniPlayer();
        setupBottomNavigation();
    }

    private void viewPlaylistSongs(Playlist playlist) {
        Intent intent = new Intent(PlaylistsActivity.this, LibraryActivity.class);
        intent.putExtra("PLAYLIST_ID", playlist.getId());
        intent.putExtra("PLAYLIST_NAME", playlist.getName());
        startActivity(intent);
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

                            if ("Liked Songs".equals(playlist.getName())) {
                                hasLikedSongs = true;
                            }

                            @SuppressWarnings("unchecked")
                            List<String> songIds = (List<String>) document.get("songIds");
                            if (songIds != null) playlist.setSongIds(songIds);

                            @SuppressWarnings("unchecked")
                            List<String> cloudSongIds = (List<String>) document.get("cloudSongIds");
                            if (cloudSongIds != null) playlist.setCloudSongIds(cloudSongIds);

                            userPlaylists.add(playlist);
                        }

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
                    loadUserPlaylists();
                });
    }

    private class PlaylistAdapter extends BaseAdapter {
        private List<Playlist> playlists;

        public PlaylistAdapter(List<Playlist> playlists) {
            this.playlists = playlists;
        }

        @Override
        public int getCount() { return playlists.size(); }
        @Override
        public Object getItem(int position) { return playlists.get(position); }
        @Override
        public long getItemId(int position) { return position; }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(PlaylistsActivity.this)
                        .inflate(R.layout.item_playlist, parent, false);
            }

            Playlist playlist = playlists.get(position);
            TextView tvName = convertView.findViewById(R.id.tvPlaylistName);
            TextView tvCount = convertView.findViewById(R.id.tvPlaylistSongCount);
            ImageButton btnDelete = convertView.findViewById(R.id.btnDeletePlaylist);
            ImageView ivPlaylistImage = convertView.findViewById(R.id.ivPlaylistImage);

            tvName.setText(playlist.getName());
            tvCount.setText(playlist.getSongCount() + " songs");

            // AICI ESTE FIX-UL PENTRU CLICK
            convertView.setOnClickListener(v -> {
                viewPlaylistSongs(playlist);
            });

            if ("Liked Songs".equals(playlist.getName())) {
                ivPlaylistImage.setImageResource(R.drawable.ic_liked_songs);
                ivPlaylistImage.setBackgroundTintList(null);
                btnDelete.setVisibility(View.GONE);
            } else {
                ivPlaylistImage.setImageResource(R.drawable.ic_playlist);
                ivPlaylistImage.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#1DB954")));
                btnDelete.setVisibility(View.VISIBLE);

                btnDelete.setOnClickListener(v -> {
                    new AlertDialog.Builder(PlaylistsActivity.this)
                            .setTitle("Delete Playlist")
                            .setMessage("Are you sure you want to delete \"" + playlist.getName() + "\"?")
                            .setPositiveButton("Delete", (dialog, which) -> deletePlaylist(playlist))
                            .setNegativeButton("Cancel", null)
                            .show();
                });
            }

            return convertView;
        }
    }

    private void deletePlaylist(Playlist playlist) {
        if (playlist.getId() == null) return;
        db.collection("playlists").document(playlist.getId())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Playlist deleted", Toast.LENGTH_SHORT).show();
                    loadUserPlaylists();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Error deleting playlist", Toast.LENGTH_SHORT).show());
    }

    private void showCreatePlaylistDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_create_playlist, null);
        TextInputEditText etPlaylistName = dialogView.findViewById(R.id.etPlaylistName);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Create New Playlist")
                .setView(dialogView)
                .setPositiveButton("Create", null)
                .setNegativeButton("Cancel", null)
                .create();

        dialog.setOnShowListener(dialogInterface -> {
            Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            positiveButton.setOnClickListener(v -> {
                String playlistName = etPlaylistName.getText().toString().trim();
                if (playlistName.isEmpty()) {
                    Toast.makeText(this, "Please enter a playlist name", Toast.LENGTH_SHORT).show();
                    return;
                }
                for (Playlist p : userPlaylists) {
                    if (playlistName.equals(p.getName())) {
                        Toast.makeText(this, "A playlist with this name already exists", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
                createPlaylist(playlistName);
                dialog.dismiss();
            });
        });

        dialog.show();
    }

    private void createPlaylist(String name) {
        String userId = sessionManager.getUserId();
        if (userId == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }
        Map<String, Object> playlist = new HashMap<>();
        playlist.put("name", name);
        playlist.put("userId", userId);
        playlist.put("songIds", new ArrayList<String>());
        playlist.put("cloudSongIds", new ArrayList<String>());
        playlist.put("createdAt", System.currentTimeMillis());

        db.collection("playlists")
                .add(playlist)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Playlist created!", Toast.LENGTH_SHORT).show();
                    loadUserPlaylists();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to create playlist: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}