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

public class LibraryActivity extends AppCompatActivity {

    private List<Song> displayedSongs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_library);

        ListView listView = findViewById(R.id.lvAllSongs);
        TextView title = findViewById(R.id.tvLibraryTitle); // We will add this ID to XML later

        // CHECK IF WE HAVE A GENRE FILTER
        String genreFilter = getIntent().getStringExtra("GENRE_FILTER");

        if (genreFilter != null) {
            // Show only specific genre
            displayedSongs = MusicLibrary.getSongsByGenre(genreFilter);
            if (title != null) title.setText(genreFilter + " Playlist");
        } else {
            // Show everything
            displayedSongs = MusicLibrary.getSongList();
        }

        LibraryAdapter adapter = new LibraryAdapter(displayedSongs);
        listView.setAdapter(adapter);

        // Handle Click
        listView.setOnItemClickListener((parent, view, position, id) -> {
            Song selectedSong = displayedSongs.get(position);

            // We need to find the GLOBAL index of this song to play it correctly
            List<Song> fullList = MusicLibrary.getSongList();
            int globalIndex = 0;
            for(int i=0; i<fullList.size(); i++) {
                if(fullList.get(i) == selectedSong) {
                    globalIndex = i;
                    break;
                }
            }

            Intent intent = new Intent(LibraryActivity.this, MainActivity.class);
            intent.putExtra("SONG_INDEX", globalIndex);
            startActivity(intent);
        });
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