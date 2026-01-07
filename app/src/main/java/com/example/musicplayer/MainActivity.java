package com.example.musicplayer;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    // UI Components
    private TextView tvSongTitle, tvArtist, tvCurrentTime, tvTotalTime;
    private ImageView albumArt;
    private SeekBar seekBar;
    private ImageButton btnPlayPause, btnNext, btnPrev, btnShuffle, btnRepeat;

    // SeekBar Updater
    private Handler handler = new Handler();
    private Runnable updateSeekBarRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Handle Window Insets (Edge-to-Edge)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // 1. Initialize Views
        initViews();

        // 2. Setup Listener: When the song changes in the background, update this screen
        MusicPlayerManager.getInstance().setOnSongChangedListener(this::updateUI);

        // 3. Setup Button Actions (Delegate everything to the Manager)
        setupClickListeners();

        // 4. Setup SeekBar Logic
        setupSeekBar();

        // 5. Initial UI Sync (in case a song is already playing)
        updateUI();

        // 6. Start the timer to move the SeekBar
        startSeekBarUpdater();
    }

    private void initViews() {
        tvSongTitle = findViewById(R.id.tvSongTitle);
        tvArtist = findViewById(R.id.tvArtist);
        tvCurrentTime = findViewById(R.id.tvCurrentTime);
        tvTotalTime = findViewById(R.id.tvTotalTime);
        albumArt = findViewById(R.id.albumArt);
        seekBar = findViewById(R.id.seekBar);
        btnPlayPause = findViewById(R.id.btnPlayPause);
        btnNext = findViewById(R.id.btnNext);
        btnPrev = findViewById(R.id.btnPrev);
        btnShuffle = findViewById(R.id.btnShuffle);
        btnRepeat = findViewById(R.id.btnRepeat);
    }

    private void setupClickListeners() {
        // Play/Pause
        btnPlayPause.setOnClickListener(v -> {
            MusicPlayerManager.getInstance().togglePlayPause();
            updatePlayPauseButton(); // Update icon immediately
        });

        // Next
        btnNext.setOnClickListener(v -> MusicPlayerManager.getInstance().playNext(this));

        // Previous
        btnPrev.setOnClickListener(v -> MusicPlayerManager.getInstance().playPrevious(this));

        // Shuffle
        btnShuffle.setOnClickListener(v -> {
            boolean newState = MusicPlayerManager.getInstance().toggleShuffle();
            btnShuffle.setAlpha(newState ? 1.0f : 0.5f); // Visual feedback
        });

        // Repeat
        btnRepeat.setOnClickListener(v -> {
            boolean newState = MusicPlayerManager.getInstance().toggleRepeat();
            btnRepeat.setAlpha(newState ? 1.0f : 0.5f); // Visual feedback
        });

        // Click Artist Name -> Open Artist Page
        tvArtist.setOnClickListener(v -> {
            Song current = MusicPlayerManager.getInstance().getCurrentSong();
            if (current != null) {
                Intent intent = new Intent(MainActivity.this, ArtistActivity.class);
                intent.putExtra("ARTIST_NAME", current.getArtist());
                startActivity(intent);
            }
        });
    }

    private void setupSeekBar() {
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    // Only seek if the user dragged it
                    MediaPlayer mp = MusicPlayerManager.getInstance().getMediaPlayer();
                    if (mp != null) {
                        mp.seekTo(progress);
                        tvCurrentTime.setText(formatTime(progress));
                    }
                }
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    private void updateUI() {
        // 1. Get current data from Manager
        Song currentSong = MusicPlayerManager.getInstance().getCurrentSong();
        MediaPlayer mp = MusicPlayerManager.getInstance().getMediaPlayer();

        if (currentSong == null) return;

        // 2. Update Text and Images
        tvSongTitle.setText(currentSong.getTitle());
        tvArtist.setText(currentSong.getArtist());

        int resId = ArtistImageHelper.getArtistImageResource(this, currentSong.getArtist());
        albumArt.setImageResource(resId);

        // 3. Update Play/Pause Icon
        updatePlayPauseButton();

        // 4. Update SeekBar Duration
        if (mp != null) {
            int duration = mp.getDuration();
            seekBar.setMax(duration);
            tvTotalTime.setText(formatTime(duration));
        }

        // 5. Sync Shuffle/Repeat Buttons
        btnShuffle.setAlpha(MusicPlayerManager.getInstance().isShuffleEnabled() ? 1.0f : 0.5f);
        btnRepeat.setAlpha(MusicPlayerManager.getInstance().isRepeatEnabled() ? 1.0f : 0.5f);
    }

    private void updatePlayPauseButton() {
        if (MusicPlayerManager.getInstance().isPlaying()) {
            btnPlayPause.setImageResource(android.R.drawable.ic_media_pause);
        } else {
            btnPlayPause.setImageResource(android.R.drawable.ic_media_play);
        }
    }

    private void startSeekBarUpdater() {
        updateSeekBarRunnable = new Runnable() {
            @Override
            public void run() {
                MediaPlayer mp = MusicPlayerManager.getInstance().getMediaPlayer();
                if (mp != null && mp.isPlaying()) {
                    int currentPosition = mp.getCurrentPosition();
                    seekBar.setProgress(currentPosition);
                    tvCurrentTime.setText(formatTime(currentPosition));
                }
                // Run this again in 1 second
                handler.postDelayed(this, 1000);
            }
        };
        handler.post(updateSeekBarRunnable);
    }

    private String formatTime(int millis) {
        int seconds = (millis / 1000) % 60;
        int minutes = (millis / 1000) / 60;
        return String.format(Locale.getDefault(), "%d:%02d", minutes, seconds);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Stop updating the seekbar when we leave the screen to save battery
        handler.removeCallbacks(updateSeekBarRunnable);
    }
}