package com.example.musicplayer;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_genres);

        GridView gridView = findViewById(R.id.gvGenres);
        GenreAdapter adapter = new GenreAdapter();
        gridView.setAdapter(adapter);

        gridView.setOnItemClickListener((parent, view, position, id) -> {
            String selectedGenre = GENRE_NAMES[position];
            String selectedImage = GENRE_IMAGES[position]; // Get the image name

            Intent intent = new Intent(GenreActivity.this, LibraryActivity.class);
            intent.putExtra("GENRE_FILTER", selectedGenre);
            intent.putExtra("GENRE_IMAGE", selectedImage); // Pass it to the next screen
            startActivity(intent);
        });

        setupMiniPlayer();
        setupBottomNavigation();
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
}