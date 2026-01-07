package com.example.musicplayer;

import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private TextView tvSongTitle, tvArtist, tvCurrentTime, tvTotalTime;
    private ImageView albumArt;
    private SeekBar seekBar;
    private ImageButton btnPlayPause, btnNext, btnPrev, btnShuffle, btnRepeat;

    private Handler handler = new Handler();
    private Runnable updateSeekBarRunnable;

    // Listener reference
    private final Runnable musicListener = this::updateUI;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // 1. Load Theme
        SharedPreferences prefs = getSharedPreferences("AppConfig", MODE_PRIVATE);
        boolean isDarkMode = prefs.getBoolean("DarkMode", false);
        AppCompatDelegate.setDefaultNightMode(isDarkMode ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);

        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // 2. Setup Swipe Down Listener on the Root View
        View rootView = findViewById(R.id.main);
        rootView.setOnTouchListener(new OnSwipeTouchListener(MainActivity.this) {
            @Override
            public void onSwipeDown() {
                // Finish this activity to go back to the previous one
                finish();
                // Optional: Add a smooth slide down animation
                overridePendingTransition(0, android.R.anim.fade_out);
            }
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initViews();
        setupClickListeners();
        setupSeekBar();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Start listening
        MusicPlayerManager.getInstance().addSongChangedListener(musicListener);
        // Sync UI
        updateUI();
        // Start SeekBar
        startSeekBarUpdater();
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Stop listening
        MusicPlayerManager.getInstance().removeSongChangedListener(musicListener);
        // Stop SeekBar
        handler.removeCallbacks(updateSeekBarRunnable);
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
        btnPlayPause.setOnClickListener(v -> {
            MusicPlayerManager.getInstance().togglePlayPause();
            // Listener will handle UI update
        });

        btnNext.setOnClickListener(v -> MusicPlayerManager.getInstance().playNext(this));
        btnPrev.setOnClickListener(v -> MusicPlayerManager.getInstance().playPrevious(this));

        btnShuffle.setOnClickListener(v -> {
            boolean newState = MusicPlayerManager.getInstance().toggleShuffle();
            btnShuffle.setAlpha(newState ? 1.0f : 0.5f);
        });

        btnRepeat.setOnClickListener(v -> {
            boolean newState = MusicPlayerManager.getInstance().toggleRepeat();
            btnRepeat.setAlpha(newState ? 1.0f : 0.5f);
        });

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
        Song currentSong = MusicPlayerManager.getInstance().getCurrentSong();
        MediaPlayer mp = MusicPlayerManager.getInstance().getMediaPlayer();

        if (currentSong == null) return;

        tvSongTitle.setText(currentSong.getTitle());
        tvArtist.setText(currentSong.getArtist());

        int resId = ArtistImageHelper.getArtistImageResource(this, currentSong.getArtist());
        albumArt.setImageResource(resId);

        updatePlayPauseButton();

        if (mp != null) {
            int duration = mp.getDuration();
            seekBar.setMax(duration);
            tvTotalTime.setText(formatTime(duration));
        }

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
        handler.removeCallbacks(updateSeekBarRunnable); // Prevent duplicates
        updateSeekBarRunnable = new Runnable() {
            @Override
            public void run() {
                MediaPlayer mp = MusicPlayerManager.getInstance().getMediaPlayer();
                if (mp != null && mp.isPlaying()) {
                    int currentPosition = mp.getCurrentPosition();
                    seekBar.setProgress(currentPosition);
                    tvCurrentTime.setText(formatTime(currentPosition));
                }
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
}