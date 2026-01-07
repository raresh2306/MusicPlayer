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

import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.Stack; // Import Stack for History

public class MainActivity extends AppCompatActivity {

    private MediaPlayer mediaPlayer;
    private TextView tvSongTitle, tvArtist, tvCurrentTime, tvTotalTime;
    private ImageView albumArt;
    private SeekBar seekBar;
    private ImageButton btnPlayPause, btnNext, btnPrev, btnShuffle, btnRepeat;

    private List<Song> songList;
    private int currentIndex = 0;
    private boolean isShuffle = false;
    private boolean isRepeat = false;
    private Handler handler = new Handler();
    private Runnable updateSeekBarRunnable;

    // HISTORY STACK to remember where we came from
    private Stack<Integer> playHistory = new Stack<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize Views
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

        // Load songs
        songList = MusicLibrary.getSongList();

        // Check for specific song start
        int passedIndex = getIntent().getIntExtra("SONG_INDEX", -1);
        if (passedIndex != -1) {
            currentIndex = passedIndex;
        }

        if (!songList.isEmpty()) {
            setupMediaPlayer(currentIndex);
            if (passedIndex != -1) {
                mediaPlayer.start();
                btnPlayPause.setImageResource(android.R.drawable.ic_media_pause);
            }
        }

        tvArtist.setOnClickListener(v -> {
            if (!songList.isEmpty()) {
                String artistName = songList.get(currentIndex).getArtist();
                Intent intent = new Intent(MainActivity.this, ArtistActivity.class);
                intent.putExtra("ARTIST_NAME", artistName);
                startActivity(intent);
            }
        });

        btnPlayPause.setOnClickListener(v -> togglePlayPause());
        btnNext.setOnClickListener(v -> playNext());

        // PREVIOUS BUTTON LOGIC
        btnPrev.setOnClickListener(v -> {
            // 1. If we are more than 3 seconds in, just restart the song
            if (mediaPlayer != null && mediaPlayer.getCurrentPosition() > 3000) {
                mediaPlayer.seekTo(0);
            }
            // 2. Otherwise, actually go to the previous track
            else {
                playPrevious();
            }
        });

        btnShuffle.setOnClickListener(v -> {
            isShuffle = !isShuffle;
            btnShuffle.setAlpha(isShuffle ? 1.0f : 0.5f);
        });

        btnRepeat.setOnClickListener(v -> {
            isRepeat = !isRepeat;
            btnRepeat.setAlpha(isRepeat ? 1.0f : 0.5f);
        });

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && mediaPlayer != null) {
                    mediaPlayer.seekTo(progress);
                }
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        updateSeekBarRunnable = new Runnable() {
            @Override
            public void run() {
                if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                    int currentPosition = mediaPlayer.getCurrentPosition();
                    seekBar.setProgress(currentPosition);
                    tvCurrentTime.setText(formatTime(currentPosition));
                }
                handler.postDelayed(this, 1000);
            }
        };
        handler.postDelayed(updateSeekBarRunnable, 1000);
    }

    private void setupMediaPlayer(int index) {
        if (mediaPlayer != null) {
            mediaPlayer.release();
        }
        Song currentSong = songList.get(index);
        mediaPlayer = MediaPlayer.create(this, currentSong.getResId());

        tvSongTitle.setText(currentSong.getTitle());
        tvArtist.setText(currentSong.getArtist());

        int artistImageRes = ArtistImageHelper.getArtistImageResource(this, currentSong.getArtist());
        albumArt.setImageResource(artistImageRes);

        int duration = mediaPlayer.getDuration();
        seekBar.setMax(duration);
        tvTotalTime.setText(formatTime(duration));
        tvCurrentTime.setText(formatTime(0));

        mediaPlayer.setOnCompletionListener(mp -> {
            if (isRepeat) {
                mediaPlayer.start();
            } else {
                playNext();
            }
        });
    }

    private void togglePlayPause() {
        if (mediaPlayer == null) return;
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            btnPlayPause.setImageResource(android.R.drawable.ic_media_play);
        } else {
            mediaPlayer.start();
            btnPlayPause.setImageResource(android.R.drawable.ic_media_pause);
        }
    }

    private void playNext() {
        if (songList.isEmpty()) return;

        // SAVE CURRENT SONG TO HISTORY BEFORE MOVING
        playHistory.push(currentIndex);

        if (isShuffle) {
            currentIndex = new Random().nextInt(songList.size());
        } else {
            currentIndex = (currentIndex + 1) % songList.size();
        }
        setupMediaPlayer(currentIndex);
        mediaPlayer.start();
        btnPlayPause.setImageResource(android.R.drawable.ic_media_pause);
    }

    private void playPrevious() {
        if (songList.isEmpty()) return;

        // CHECK HISTORY FIRST
        if (!playHistory.isEmpty()) {
            // Go back to the song we were just listening to
            currentIndex = playHistory.pop();
        } else {
            // If no history, use the standard linear previous (index - 1)
            currentIndex = (currentIndex - 1 + songList.size()) % songList.size();
        }

        setupMediaPlayer(currentIndex);
        mediaPlayer.start();
        btnPlayPause.setImageResource(android.R.drawable.ic_media_pause);
    }

    private String formatTime(int millis) {
        int seconds = (millis / 1000) % 60;
        int minutes = (millis / 1000) / 60;
        return String.format(Locale.getDefault(), "%d:%02d", minutes, seconds);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        handler.removeCallbacks(updateSeekBarRunnable);
    }
}