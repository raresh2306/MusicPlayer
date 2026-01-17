package com.example.musicplayer;

import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private TextView tvSongTitle, tvArtist, tvCurrentTime, tvTotalTime;
    private ImageView albumArt;
    private SeekBar seekBar;
    private ImageButton btnPlayPause, btnNext, btnPrev, btnShuffle, btnRepeat, btnAddToPlaylist, btnLike;

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
        btnAddToPlaylist = findViewById(R.id.btnAddToPlaylist);
        btnLike = findViewById(R.id.btnLike);
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

        btnAddToPlaylist.setOnClickListener(v -> {
            Song current = MusicPlayerManager.getInstance().getCurrentSong();
            if (current != null) {
                showAddToPlaylistDialog(current);
            } else {
                Toast.makeText(this, "No song playing", Toast.LENGTH_SHORT).show();
            }
        });

        btnLike.setOnClickListener(v -> {
            Song current = MusicPlayerManager.getInstance().getCurrentSong();
            if (current != null) {
                toggleLikeSong(current);
            } else {
                Toast.makeText(this, "No song playing", Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void showAddToPlaylistDialog(Song song) {
        String userId = FirebaseAuth.getInstance().getCurrentUser() != null ? 
            FirebaseAuth.getInstance().getCurrentUser().getUid() : null;
        
        if (userId == null) {
            Toast.makeText(this, "Please login to add songs to playlists", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("playlists")
            .whereEqualTo("userId", userId)
            .get()
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    List<Playlist> playlists = new ArrayList<>();
                    List<String> playlistNames = new ArrayList<>();
                    
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        Playlist playlist = new Playlist();
                        playlist.setId(document.getId());
                        playlist.setName(document.getString("name"));
                        playlist.setUserId(document.getString("userId"));
                        
                        @SuppressWarnings("unchecked")
                        List<String> songIds = (List<String>) document.get("songIds");
                        if (songIds != null) {
                            playlist.setSongIds(songIds);
                        }
                        
                        playlists.add(playlist);
                        playlistNames.add(playlist.getName());
                    }
                    
                    if (playlists.isEmpty()) {
                        Toast.makeText(this, "No playlists found. Create one in your profile!", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    
                    String[] playlistArray = playlistNames.toArray(new String[0]);
                    
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Add to Playlist")
                        .setItems(playlistArray, (dialog, which) -> {
                            Playlist selectedPlaylist = playlists.get(which);
                            addSongToPlaylist(selectedPlaylist, song);
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
                } else {
                    Toast.makeText(this, "Failed to load playlists", Toast.LENGTH_SHORT).show();
                }
            });
    }
    
    private void addSongToPlaylist(Playlist playlist, Song song) {
        if (playlist.getId() == null) return;
        
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        
        if (song.isCloudSong()) {
            // Pentru melodiile cloud
            String userId = FirebaseAuth.getInstance().getCurrentUser() != null ? 
                FirebaseAuth.getInstance().getCurrentUser().getUid() : null;
            
            if (userId == null) {
                Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
                return;
            }
            
            // Obține ID-ul melodiei cloud din Firestore
            db.collection("cloud_songs")
                .whereEqualTo("userId", userId)
                .whereEqualTo("cloudUrl", song.getCloudUrl())
                .limit(1)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null && !task.getResult().isEmpty()) {
                        String cloudSongId = task.getResult().getDocuments().get(0).getId();
                        List<String> cloudSongIds = new ArrayList<>(playlist.getCloudSongIds());
                        
                        if (cloudSongIds.contains(cloudSongId)) {
                            Toast.makeText(MainActivity.this, "Song already in playlist", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        
                        cloudSongIds.add(cloudSongId);
                        
                        db.collection("playlists").document(playlist.getId())
                            .update("cloudSongIds", cloudSongIds)
                            .addOnCompleteListener(updateTask -> {
                                if (updateTask.isSuccessful()) {
                                    Toast.makeText(MainActivity.this, "Added to " + playlist.getName(), Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(MainActivity.this, "Failed to add song to playlist", Toast.LENGTH_SHORT).show();
                                }
                            });
                    } else {
                        Toast.makeText(MainActivity.this, "Cloud song not found", Toast.LENGTH_SHORT).show();
                    }
                });
        } else {
            // Pentru melodiile locale
            String songId = String.valueOf(song.getResId());
            List<String> songIds = new ArrayList<>(playlist.getSongIds());
            
            // Verifică dacă melodia nu este deja în playlist
            if (songIds.contains(songId)) {
                Toast.makeText(this, "Song already in playlist", Toast.LENGTH_SHORT).show();
                return;
            }
            
            songIds.add(songId);
            
            db.collection("playlists").document(playlist.getId())
                .update("songIds", songIds)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Added to " + playlist.getName(), Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Failed to add song to playlist", Toast.LENGTH_SHORT).show();
                    }
                });
        }
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
        
        // Actualizează starea butonului de like
        updateLikeButton(currentSong);

        // Verifică dacă melodia are cover image din cloud
        String coverImageUrl = currentSong.getCoverImageUrl();
        if (coverImageUrl != null && !coverImageUrl.trim().isEmpty()) {
            // Încarcă imaginea din URL folosind Glide
            Glide.with(this)
                .load(coverImageUrl)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .placeholder(ArtistImageHelper.getArtistImageResource(this, currentSong.getArtist()))
                .error(ArtistImageHelper.getArtistImageResource(this, currentSong.getArtist()))
                .into(albumArt);
        } else {
            // Folosește imaginea default bazată pe artist
            int resId = ArtistImageHelper.getArtistImageResource(this, currentSong.getArtist());
            albumArt.setImageResource(resId);
        }

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
    
    private void toggleLikeSong(Song song) {
        String userId = FirebaseAuth.getInstance().getCurrentUser() != null ? 
            FirebaseAuth.getInstance().getCurrentUser().getUid() : null;
        
        if (userId == null) {
            Toast.makeText(this, "Please login to like songs", Toast.LENGTH_SHORT).show();
            return;
        }
        
        LikedSongsHelper.toggleLikeSong(this, song, new LikedSongsHelper.OnLikeToggled() {
            @Override
            public void onToggled(boolean isLiked, String message) {
                Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
                updateLikeButton(song);
            }
            
            @Override
            public void onError(String error) {
                Toast.makeText(MainActivity.this, error, Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void updateLikeButton(Song song) {
        String userId = FirebaseAuth.getInstance().getCurrentUser() != null ? 
            FirebaseAuth.getInstance().getCurrentUser().getUid() : null;
        
        if (userId == null || song == null) {
            btnLike.setImageResource(R.drawable.ic_heart);
            btnLike.setAlpha(0.5f);
            return;
        }
        
        LikedSongsHelper.checkIfLiked(userId, song, new LikedSongsHelper.OnLikeStatusChecked() {
            @Override
            public void onChecked(boolean isLiked) {
                if (isLiked) {
                    btnLike.setImageResource(R.drawable.ic_heart_filled);
                    btnLike.setAlpha(1.0f);
                } else {
                    btnLike.setImageResource(R.drawable.ic_heart);
                    btnLike.setAlpha(0.7f);
                }
            }
        });
    }
}