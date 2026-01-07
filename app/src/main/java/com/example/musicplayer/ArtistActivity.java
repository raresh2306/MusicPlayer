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

import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

public class ArtistActivity extends AppCompatActivity {

    private ImageView ivArtistImage;
    private TextView tvArtistName;
    private ListView lvArtistSongs;
    private List<Song> artistSongs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_artist);

        ivArtistImage = findViewById(R.id.ivArtistImage);
        tvArtistName = findViewById(R.id.tvArtistName);
        lvArtistSongs = findViewById(R.id.lvArtistSongs);

        // Get artist name from intent
        String artistName = getIntent().getStringExtra("ARTIST_NAME");
        if (artistName == null) {
            artistName = "Unknown Artist";
        }

        tvArtistName.setText(artistName);
        
        // Set artist image
        int artistImageRes = ArtistImageHelper.getArtistImageResource(this, artistName);
        ivArtistImage.setImageResource(artistImageRes);

        // Get songs by this artist
        artistSongs = MusicLibrary.getSongsByArtist(artistName);

        // Create custom adapter for ListView
        SongAdapter adapter = new SongAdapter(artistSongs);
        lvArtistSongs.setAdapter(adapter);
    }

    private class SongAdapter extends BaseAdapter {
        private List<Song> songs;

        public SongAdapter(List<Song> songs) {
            this.songs = songs;
        }

        @Override
        public int getCount() {
            return songs.size();
        }

        @Override
        public Object getItem(int position) {
            return songs.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

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

