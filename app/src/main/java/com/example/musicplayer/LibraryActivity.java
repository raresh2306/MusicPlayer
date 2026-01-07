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

        // 1. Setup Data and UI
        if (genreFilter != null) {
            // Case A: Opened from Genres Page (Filtered)
            displayedSongs = MusicLibrary.getSongsByGenre(genreFilter);
            title.setText(genreFilter);

            if (genreImageName != null) {
                int resId = getResources().getIdentifier(genreImageName, "drawable", getPackageName());
                if (resId != 0) playlistImage.setImageResource(resId);
            }
        } else {
            // Case B: Opened "Music Library" (All Songs)
            displayedSongs = MusicLibrary.getSongList();
            title.setText("All Music");

            // NEW: Set the specific image for the main library
            int resId = getResources().getIdentifier("library_cover", "drawable", getPackageName());
            if (resId != 0) {
                playlistImage.setImageResource(resId);
            }
        }

        LibraryAdapter adapter = new LibraryAdapter(displayedSongs);
        listView.setAdapter(adapter);

        // 2. Play Button Logic
        btnPlay.setOnClickListener(v -> {
            if (!displayedSongs.isEmpty()) {
                MusicPlayerManager.getInstance().playSong(LibraryActivity.this, displayedSongs, 0);
            }
        });

        // 3. List Click Logic
        listView.setOnItemClickListener((parent, view, position, id) -> {
            MusicPlayerManager.getInstance().playSong(LibraryActivity.this, displayedSongs, position);
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