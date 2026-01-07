package com.example.musicplayer;

import android.content.Context;
import android.media.MediaPlayer;
import java.util.ArrayList;
import java.util.List;

public class MusicPlayerManager {
    public boolean toggleShuffle() {
        isShuffle = !isShuffle;
        return isShuffle;
    }

    public boolean toggleRepeat() {
        isRepeat = !isRepeat;
        return isRepeat;
    }

    public boolean isShuffleEnabled() {
        return isShuffle;
    }

    public boolean isRepeatEnabled() {
        return isRepeat;
    }

    private static MusicPlayerManager instance;
    private MediaPlayer mediaPlayer;
    private List<Song> currentPlaylist = new ArrayList<>();
    private int currentIndex = 0;
    private boolean isShuffle = false;
    private boolean isRepeat = false;

    // Listener to tell Activities when the song changes
    private Runnable onSongChangedListener;

    private MusicPlayerManager() {
        // Private constructor for Singleton
    }

    public static synchronized MusicPlayerManager getInstance() {
        if (instance == null) {
            instance = new MusicPlayerManager();
        }
        return instance;
    }

    public void setOnSongChangedListener(Runnable listener) {
        this.onSongChangedListener = listener;
    }

    public void playSong(Context context, List<Song> songs, int index) {
        // Update playlist context
        this.currentPlaylist = new ArrayList<>(songs);
        this.currentIndex = index;

        playCurrentSong(context);
    }

    private void playCurrentSong(Context context) {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
        }

        if (currentPlaylist.isEmpty()) return;

        Song song = currentPlaylist.get(currentIndex);
        mediaPlayer = MediaPlayer.create(context, song.getResId());

        mediaPlayer.setOnCompletionListener(mp -> {
            if (isRepeat) {
                playCurrentSong(context);
            } else {
                playNext(context);
            }
        });

        mediaPlayer.start();
        notifyUI();
    }

    public void togglePlayPause() {
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
            } else {
                mediaPlayer.start();
            }
            notifyUI();
        }
    }

    public void playNext(Context context) {
        if (currentPlaylist.isEmpty()) return;

        if (isShuffle) {
            currentIndex = (int) (Math.random() * currentPlaylist.size());
        } else {
            currentIndex = (currentIndex + 1) % currentPlaylist.size();
        }
        playCurrentSong(context);
    }

    public void playPrevious(Context context) {
        if (currentPlaylist.isEmpty()) return;
        currentIndex = (currentIndex - 1 + currentPlaylist.size()) % currentPlaylist.size();
        playCurrentSong(context);
    }

    private void notifyUI() {
        if (onSongChangedListener != null) {
            onSongChangedListener.run();
        }
    }

    // --- Getters for UI ---
    public boolean isPlaying() {
        return mediaPlayer != null && mediaPlayer.isPlaying();
    }

    public Song getCurrentSong() {
        if (currentPlaylist.isEmpty() || currentIndex < 0 || currentIndex >= currentPlaylist.size()) {
            return null;
        }
        return currentPlaylist.get(currentIndex);
    }

    public MediaPlayer getMediaPlayer() {
        return mediaPlayer;
    }
}