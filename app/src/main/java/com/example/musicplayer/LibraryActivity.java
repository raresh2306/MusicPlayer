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
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.ArrayList;
import java.util.List;

public class LibraryActivity extends BaseActivity {

    private List<Song> displayedSongs;

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
        @SuppressWarnings("unchecked")
        ArrayList<Song> playlistSongs = (ArrayList<Song>) getIntent().getSerializableExtra("PLAYLIST_SONGS");

        // 1. Setup Data and UI
        if (playlistId != null) {
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
                MusicPlayerManager.getInstance().playSong(LibraryActivity.this, displayedSongs, 0);
                startActivity(new Intent(LibraryActivity.this, MainActivity.class));
            }
        });

        // 3. List Click Logic
        listView.setOnItemClickListener((parent, view, position, id) -> {
            if (displayedSongs != null && position < displayedSongs.size()) {
                MusicPlayerManager.getInstance().playSong(LibraryActivity.this, displayedSongs, position);
                startActivity(new Intent(LibraryActivity.this, MainActivity.class));
            }
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
}