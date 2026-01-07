package com.example.musicplayer;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

public abstract class BaseActivity extends AppCompatActivity {

    // Keep a reference to the listener for the Mini Player
    private final Runnable miniPlayerListener = this::updateMiniPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // 1. Load Theme Preference
        SharedPreferences prefs = getSharedPreferences("AppConfig", MODE_PRIVATE);
        boolean isDarkMode = prefs.getBoolean("DarkMode", false);
        AppCompatDelegate.setDefaultNightMode(isDarkMode ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);

        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Update immediately
        updateMiniPlayer();
        // Start listening for changes (Instant Pop-up Fix)
        MusicPlayerManager.getInstance().addSongChangedListener(miniPlayerListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Stop listening when we leave the screen
        MusicPlayerManager.getInstance().removeSongChangedListener(miniPlayerListener);
    }

    protected void setupMiniPlayer() {
        View miniPlayer = findViewById(R.id.miniPlayerContainer);
        if (miniPlayer == null) return;

        // Click on bar opens Main Player
        miniPlayer.setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(intent);
            // NEW: Add Fade In Transition here!
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        });

        // Play/Pause button logic
        ImageButton btnPlayPause = findViewById(R.id.btnMiniPlayPause);
        btnPlayPause.setOnClickListener(v -> {
            MusicPlayerManager.getInstance().togglePlayPause();
            // Listener will handle the UI update automatically
        });
    }

    protected void setupBottomNavigation() {
        View navHome = findViewById(R.id.navHome);
        View navSearch = findViewById(R.id.navSearch);
        View navPlaylists = findViewById(R.id.navPlaylists);
        View navTheme = findViewById(R.id.navTheme);

        if (navHome != null) {
            navHome.setOnClickListener(v -> {
                if (!(this instanceof HomeActivity)) {
                    startActivity(new Intent(this, HomeActivity.class));
                    overridePendingTransition(0, 0);
                }
            });
        }

        if (navSearch != null) {
            navSearch.setOnClickListener(v -> {
                if (!(this instanceof SearchActivity)) {
                    startActivity(new Intent(this, SearchActivity.class));
                    overridePendingTransition(0, 0);
                }
            });
        }

        if (navPlaylists != null) {
            navPlaylists.setOnClickListener(v -> {
                if (!(this instanceof GenreActivity)) {
                    startActivity(new Intent(this, GenreActivity.class));
                    overridePendingTransition(0, 0);
                }
            });
        }

        if (navTheme != null) {
            navTheme.setOnClickListener(v -> toggleTheme());
        }
    }

    private void toggleTheme() {
        SharedPreferences prefs = getSharedPreferences("AppConfig", MODE_PRIVATE);
        boolean isDarkMode = prefs.getBoolean("DarkMode", false);

        // Save new state
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("DarkMode", !isDarkMode);
        editor.apply();

        // Apply and Recreate
        AppCompatDelegate.setDefaultNightMode(!isDarkMode ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);
        recreate(); // Reloads the screen to apply colors
    }

    private void updateMiniPlayer() {
        View miniPlayer = findViewById(R.id.miniPlayerContainer);
        if (miniPlayer == null) return;

        Song currentSong = MusicPlayerManager.getInstance().getCurrentSong();

        if (currentSong == null) {
            miniPlayer.setVisibility(View.GONE);
            return;
        }

        miniPlayer.setVisibility(View.VISIBLE);

        TextView title = findViewById(R.id.tvMiniTitle);
        TextView artist = findViewById(R.id.tvMiniArtist);
        ImageView art = findViewById(R.id.ivMiniAlbumArt);
        ImageButton btnPlayPause = findViewById(R.id.btnMiniPlayPause);

        title.setText(currentSong.getTitle());
        artist.setText(currentSong.getArtist());

        int resId = ArtistImageHelper.getArtistImageResource(this, currentSong.getArtist());
        art.setImageResource(resId);

        if (MusicPlayerManager.getInstance().isPlaying()) {
            btnPlayPause.setImageResource(android.R.drawable.ic_media_pause);
        } else {
            btnPlayPause.setImageResource(android.R.drawable.ic_media_play);
        }
    }
}