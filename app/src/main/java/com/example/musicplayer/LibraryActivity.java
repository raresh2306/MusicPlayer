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
    private ListView listView;
    private LibraryAdapter adapter;
    private TextView title;
    private ImageView playlistImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_library);

        listView = findViewById(R.id.lvAllSongs);
        title = findViewById(R.id.tvLibraryTitle);
        playlistImage = findViewById(R.id.ivLibraryImage);
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

        // Inițializăm lista goală ca să evităm NullPointer
        displayedSongs = new ArrayList<>();
        refreshList();

        // 1. Setup Data
        if (isAddToPlaylistMode) {
            // Modificăm și aici să fie safe
            MusicLibrary.getAllSongs(this, songs -> {
                displayedSongs = songs;
                title.setText("Add songs to: " + (playlistName != null ? playlistName : "Playlist"));
                refreshList();
            });
            int resId = getResources().getIdentifier("library_cover", "drawable", getPackageName());
            if (resId != 0) playlistImage.setImageResource(resId);

        } else if (playlistId != null) {
            // Case D: Playlist ID logic
            title.setText(playlistName != null ? playlistName : "Loading...");
            int resId = getResources().getIdentifier("library_cover", "drawable", getPackageName());
            if (resId != 0) playlistImage.setImageResource(resId);

            FirebaseFirestore.getInstance().collection("playlists").document(playlistId)
                    .get()
                    .addOnSuccessListener(document -> {
                        if (document.exists()) {
                            Playlist realPlaylist = new Playlist();
                            realPlaylist.setId(document.getId());
                            realPlaylist.setName(document.getString("name"));
                            realPlaylist.setUserId(document.getString("userId"));

                            @SuppressWarnings("unchecked")
                            List<String> songIds = (List<String>) document.get("songIds");
                            if (songIds != null) realPlaylist.setSongIds(songIds);

                            @SuppressWarnings("unchecked")
                            List<String> cloudSongIds = (List<String>) document.get("cloudSongIds");
                            if (cloudSongIds != null) realPlaylist.setCloudSongIds(cloudSongIds);

                            PlaylistHelper.getPlaylistSongs(LibraryActivity.this, realPlaylist, new PlaylistHelper.OnPlaylistSongsLoaded() {
                                @Override
                                public void onSuccess(List<Song> songs) {
                                    displayedSongs = songs;
                                    title.setText(realPlaylist.getName());
                                    refreshList();
                                }
                                @Override
                                public void onError(String error) {
                                    Toast.makeText(LibraryActivity.this, "Error: " + error, Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    });

        } else if (playlistSongs != null && !playlistSongs.isEmpty()) {
            displayedSongs = playlistSongs;
            title.setText(playlistName != null ? playlistName : "Playlist");
            int resId = getResources().getIdentifier("library_cover", "drawable", getPackageName());
            if (resId != 0) playlistImage.setImageResource(resId);
            refreshList();

        } else if (genreFilter != null) {
            displayedSongs = MusicLibrary.getSongsByGenre(this, genreFilter);
            title.setText(genreFilter);
            if (genreImageName != null) {
                int resId = getResources().getIdentifier(genreImageName, "drawable", getPackageName());
                if (resId != 0) playlistImage.setImageResource(resId);
            }
            refreshList();

        } else {
            // Case B: "All Music" - AICI ERA PROBLEMA
            // Folosim noua metodă async pentru a preveni crash-ul
            title.setText("Loading...");
            MusicLibrary.getAllSongs(this, songs -> {
                displayedSongs = songs;
                title.setText("All Music");
                refreshList();
            });

            int resId = getResources().getIdentifier("library_cover", "drawable", getPackageName());
            if (resId != 0) playlistImage.setImageResource(resId);
        }

        // 2. Play Button Logic
        btnPlay.setOnClickListener(v -> {
            if (displayedSongs != null && !displayedSongs.isEmpty()) {
                if (isAddToPlaylistMode) {
                    showAddSongsToPlaylistDialog();
                } else {
                    MusicPlayerManager.getInstance().playSong(LibraryActivity.this, displayedSongs, 0);
                }
            } else {
                if (isAddToPlaylistMode) showAddSongsToPlaylistDialog();
                else Toast.makeText(this, "List is empty or loading...", Toast.LENGTH_SHORT).show();
            }
        });

        // 3. List Click Logic
        listView.setOnItemClickListener((parent, view, position, id) -> {
            if (displayedSongs != null && position < displayedSongs.size()) {
                if (isAddToPlaylistMode) {
                    showAddSongsToPlaylistDialog();
                } else {
                    MusicPlayerManager.getInstance().playSong(LibraryActivity.this, displayedSongs, position);
                }
            }
        });

        // 4. Long-click
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

    private void refreshList() {
        if (displayedSongs == null) displayedSongs = new ArrayList<>();
        adapter = new LibraryAdapter(displayedSongs);
        listView.setAdapter(adapter);
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

            // Folosim un try-catch si aici pentru siguranta
            try {
                if (song.isCloudSong() && song.getCoverImageUrl() != null && !song.getCoverImageUrl().isEmpty()) {
                    // Daca ai Glide, foloseste-l, altfel fallback
                    int artistImageRes = ArtistImageHelper.getArtistImageResource(LibraryActivity.this, song.getArtist());
                    ivImage.setImageResource(artistImageRes);
                } else {
                    int artistImageRes = ArtistImageHelper.getArtistImageResource(LibraryActivity.this, song.getArtist());
                    ivImage.setImageResource(artistImageRes);
                }
            } catch (Exception e) {
                ivImage.setImageResource(R.drawable.ic_music_note); // Fallback generic
            }

            return convertView;
        }
    }

    private void showAddToPlaylistDialog(Song song) {
        String userId = FirebaseAuth.getInstance().getCurrentUser() != null ?
                FirebaseAuth.getInstance().getCurrentUser().getUid() : null;
        if (userId == null) {
            Toast.makeText(this, "Please login", Toast.LENGTH_SHORT).show();
            return;
        }
        FirebaseFirestore.getInstance().collection("playlists")
                .whereEqualTo("userId", userId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Playlist> playlists = new ArrayList<>();
                        List<String> playlistNames = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Playlist p = new Playlist();
                            p.setId(document.getId());
                            p.setName(document.getString("name"));
                            if (document.get("songIds") != null) p.setSongIds((List<String>)document.get("songIds"));
                            if (document.get("cloudSongIds") != null) p.setCloudSongIds((List<String>)document.get("cloudSongIds"));
                            playlists.add(p);
                            playlistNames.add(p.getName());
                        }
                        if (playlists.isEmpty()) return;
                        new AlertDialog.Builder(this)
                                .setTitle("Add to Playlist")
                                .setItems(playlistNames.toArray(new String[0]), (d, w) -> addSongToPlaylist(playlists.get(w), song))
                                .setNegativeButton("Cancel", null)
                                .show();
                    }
                });
    }

    private void addSongToPlaylist(Playlist playlist, Song song) {
        if (playlist.getId() == null) return;
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        if (song.isCloudSong()) {
            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            db.collection("cloud_songs").whereEqualTo("userId", userId).whereEqualTo("cloudUrl", song.getCloudUrl())
                    .limit(1).get().addOnSuccessListener(res -> {
                        if (!res.isEmpty()) {
                            String cid = res.getDocuments().get(0).getId();
                            List<String> cids = new ArrayList<>(playlist.getCloudSongIds());
                            if(!cids.contains(cid)) {
                                cids.add(cid);
                                db.collection("playlists").document(playlist.getId()).update("cloudSongIds", cids)
                                        .addOnSuccessListener(v -> Toast.makeText(this, "Added to playlist", Toast.LENGTH_SHORT).show());
                            }
                        }
                    });
        } else {
            List<String> songIds = new ArrayList<>(playlist.getSongIds());
            String songId = song.getStringId();
            if (!songIds.contains(songId)) {
                songIds.add(songId);
                db.collection("playlists").document(playlist.getId()).update("songIds", songIds)
                        .addOnSuccessListener(v -> Toast.makeText(this, "Added to playlist", Toast.LENGTH_SHORT).show());
            }
        }
    }

    private void showAddSongsToPlaylistDialog() {
        if (targetPlaylistId == null || displayedSongs == null || displayedSongs.isEmpty()) return;

        FirebaseFirestore.getInstance().collection("playlists").document(targetPlaylistId)
                .get().addOnSuccessListener(doc -> {
                    Playlist p = new Playlist();
                    p.setId(doc.getId());
                    p.setName(targetPlaylistName);
                    if (doc.get("songIds") != null) p.setSongIds((List<String>)doc.get("songIds"));
                    if (doc.get("cloudSongIds") != null) p.setCloudSongIds((List<String>)doc.get("cloudSongIds"));

                    String[] names = new String[displayedSongs.size()];
                    boolean[] checked = new boolean[displayedSongs.size()];
                    for(int i=0; i<displayedSongs.size(); i++) {
                        Song s = displayedSongs.get(i);
                        names[i] = s.getTitle();
                        if(!s.isCloudSong()) checked[i] = p.getSongIds().contains(s.getStringId());
                    }

                    new AlertDialog.Builder(this)
                            .setMultiChoiceItems(names, checked, (d,w,c) -> checked[w]=c)
                            .setPositiveButton("Add", (d,w) -> {
                                List<Song> toAdd = new ArrayList<>();
                                for(int i=0; i<checked.length; i++) if(checked[i]) toAdd.add(displayedSongs.get(i));
                                addSongsToPlaylist(p, toAdd);
                            })
                            .setNegativeButton("Cancel", null)
                            .show();
                });
    }

    private void addSongsToPlaylist(Playlist playlist, List<Song> songs) {
        if (playlist.getId() == null) return;
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        List<String> songIds = new ArrayList<>(playlist.getSongIds());

        for (Song s : songs) {
            if (!s.isCloudSong()) {
                String sid = s.getStringId();
                if (!songIds.contains(sid)) songIds.add(sid);
            }
        }

        db.collection("playlists").document(playlist.getId())
                .update("songIds", songIds)
                .addOnSuccessListener(v -> {
                    Toast.makeText(this, "Playlist Updated", Toast.LENGTH_SHORT).show();
                    finish();
                });
    }
}