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

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

public abstract class BaseActivity extends AppCompatActivity {

    // Keep a reference to the listener for the Mini Player
    private final Runnable miniPlayerListener = this::updateMiniPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // 1. Check if user is logged in (except for LoginActivity and SignUpActivity)
        String className = this.getClass().getSimpleName();
        if (!className.equals("LoginActivity") && !className.equals("SignUpActivity")) {
            SessionManager sessionManager = new SessionManager(this);
            if (!sessionManager.isLoggedIn()) {
                startActivity(new android.content.Intent(this, LoginActivity.class));
                finish();
                return;
            }
        }
        
        // 2. Load Theme Preference
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
        View navAccount = findViewById(R.id.navAccount);

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
                if (!(this instanceof PlaylistsActivity)) {
                    startActivity(new Intent(this, PlaylistsActivity.class));
                    overridePendingTransition(0, 0);
                }
            });
        }

        if (navTheme != null) {
            navTheme.setOnClickListener(v -> toggleTheme());
        }

        if (navAccount != null) {
            navAccount.setOnClickListener(v -> {
                if (!(this instanceof ProfileActivity)) {
                    startActivity(new Intent(this, ProfileActivity.class));
                    overridePendingTransition(0, 0);
                }
            });
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

        // Verifică dacă melodia are cover image din cloud
        String coverImageUrl = currentSong.getCoverImageUrl();
        if (coverImageUrl != null && !coverImageUrl.trim().isEmpty()) {
            // Încarcă imaginea din URL folosind Glide
            Glide.with(this)
                .load(coverImageUrl)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .placeholder(ArtistImageHelper.getArtistImageResource(this, currentSong.getArtist()))
                .error(ArtistImageHelper.getArtistImageResource(this, currentSong.getArtist()))
                .into(art);
        } else {
            // Folosește imaginea default bazată pe artist
            int resId = ArtistImageHelper.getArtistImageResource(this, currentSong.getArtist());
            art.setImageResource(resId);
        }

        if (MusicPlayerManager.getInstance().isPlaying()) {
            btnPlayPause.setImageResource(android.R.drawable.ic_media_pause);
        } else {
            btnPlayPause.setImageResource(android.R.drawable.ic_media_play);
        }
    }
}