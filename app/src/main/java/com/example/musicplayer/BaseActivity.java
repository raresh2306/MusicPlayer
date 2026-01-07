package com.example.musicplayer;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public abstract class BaseActivity extends AppCompatActivity {

    @Override
    protected void onResume() {
        super.onResume();
        updateMiniPlayer();
    }

    // Call this method in onCreate of child activities after setContentView
    protected void setupMiniPlayer() {
        View miniPlayer = findViewById(R.id.miniPlayerContainer);
        if (miniPlayer == null) return;

        // Click on bar opens Main Player
        miniPlayer.setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT); // Important!
            startActivity(intent);
        });

        // Play/Pause button logic
        ImageButton btnPlayPause = findViewById(R.id.btnMiniPlayPause);
        btnPlayPause.setOnClickListener(v -> {
            MusicPlayerManager.getInstance().togglePlayPause();
            updateMiniPlayer();
        });

        updateMiniPlayer();
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