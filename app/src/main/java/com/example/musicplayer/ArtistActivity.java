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

public class ArtistActivity extends BaseActivity {

    private ImageView ivArtistImage;
    private TextView tvArtistName;
    private ListView lvArtistSongs;
    private List<Song> artistSongs;
    private String currentArtistName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_artist);

        ivArtistImage = findViewById(R.id.ivArtistImage);
        tvArtistName = findViewById(R.id.tvArtistName);
        lvArtistSongs = findViewById(R.id.lvArtistSongs);

        currentArtistName = getIntent().getStringExtra("ARTIST_NAME");
        if (currentArtistName == null) {
            currentArtistName = "Unknown Artist";
        }

        tvArtistName.setText(currentArtistName);

        int artistImageRes = ArtistImageHelper.getArtistImageResource(this, currentArtistName);
        ivArtistImage.setImageResource(artistImageRes);

        artistSongs = MusicLibrary.getSongsByArtist(currentArtistName);

        SongAdapter adapter = new SongAdapter(artistSongs);
        lvArtistSongs.setAdapter(adapter);

        // UPDATED: Click just plays the song, doesn't open full player
        lvArtistSongs.setOnItemClickListener((parent, view, position, id) -> {
            MusicPlayerManager.getInstance().playSong(ArtistActivity.this, artistSongs, position);
        });

        setupMiniPlayer();
        setupBottomNavigation();
    }

    private class SongAdapter extends BaseAdapter {
        private List<Song> songs;

        public SongAdapter(List<Song> songs) {
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
                convertView = LayoutInflater.from(ArtistActivity.this)
                        .inflate(R.layout.item_song, parent, false);
            }

            Song song = songs.get(position);

            TextView tvTitle = convertView.findViewById(R.id.tvSongTitleItem);
            TextView tvGenre = convertView.findViewById(R.id.tvSongGenreItem);

            tvTitle.setText(song.getTitle());
            tvGenre.setText(song.getGenre());

            return convertView;
        }
    }
}