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
import java.util.List;

// Extend BaseActivity
public class LibraryActivity extends BaseActivity {

    private List<Song> displayedSongs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_library);

        ListView listView = findViewById(R.id.lvAllSongs);
        TextView title = findViewById(R.id.tvLibraryTitle);

        String genreFilter = getIntent().getStringExtra("GENRE_FILTER");

        if (genreFilter != null) {
            displayedSongs = MusicLibrary.getSongsByGenre(genreFilter);
            if (title != null) title.setText(genreFilter + " Playlist");
        } else {
            displayedSongs = MusicLibrary.getSongList();
        }

        LibraryAdapter adapter = new LibraryAdapter(displayedSongs);
        listView.setAdapter(adapter);

        // UPDATED: Use MusicPlayerManager instead of passing messy Intents
        listView.setOnItemClickListener((parent, view, position, id) -> {
            // 1. Play the specific list of songs (Filtered or All)
            MusicPlayerManager.getInstance().playSong(LibraryActivity.this, displayedSongs, position);

            // 2. Open MainActivity (which now acts as a UI viewer)
            Intent intent = new Intent(LibraryActivity.this, MainActivity.class);
            startActivity(intent);
        });

        // Initialize the Mini Player
        setupMiniPlayer();
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