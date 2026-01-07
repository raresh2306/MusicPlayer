package com.example.musicplayer;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

// Extend BaseActivity instead of AppCompatActivity
public class HomeActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        Button btnLib = findViewById(R.id.btnGoLibrary);
        Button btnGen = findViewById(R.id.btnGoGenres);

        btnLib.setOnClickListener(v -> {
            startActivity(new Intent(HomeActivity.this, LibraryActivity.class));
        });

        btnGen.setOnClickListener(v -> {
            startActivity(new Intent(HomeActivity.this, GenreActivity.class));
        });

        // Initialize the Mini Player
        setupMiniPlayer();
    }
}