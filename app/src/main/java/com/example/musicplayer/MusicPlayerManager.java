package com.example.musicplayer;

import android.content.Context;
import android.media.MediaPlayer;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack; // Import Stack

public class MusicPlayerManager {

    private static MusicPlayerManager instance;
    private MediaPlayer mediaPlayer;
    private List<Song> currentPlaylist = new ArrayList<>();
    private int currentIndex = 0;
    private boolean isShuffle = false;
    private boolean isRepeat = false;
    private List<Runnable> listeners = new ArrayList<>();

    // History Stack to remember played songs
    private Stack<Integer> songHistory = new Stack<>();

    private MusicPlayerManager() {}

    public static synchronized MusicPlayerManager getInstance() {
        if (instance == null) instance = new MusicPlayerManager();
        return instance;
    }

    public void addSongChangedListener(Runnable listener) {
        if (!listeners.contains(listener)) listeners.add(listener);
    }

    public void removeSongChangedListener(Runnable listener) {
        listeners.remove(listener);
    }

    public void playSong(Context context, List<Song> songs, int index) {
        // If we are switching to a completely new playlist, clear history
        if (!songs.equals(currentPlaylist)) {
            songHistory.clear();
        } else {
            // If just jumping around same list, save current spot before moving
            if (!currentPlaylist.isEmpty()) {
                songHistory.push(currentIndex);
            }
        }

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

        // REVERTED: Standard resource playback only (No Audio URI)
        mediaPlayer = MediaPlayer.create(context, song.getResId());

        if (mediaPlayer != null) {
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
    }

    public void togglePlayPause() {
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) mediaPlayer.pause();
            else mediaPlayer.start();
            notifyUI();
        }
    }

    public void playNext(Context context) {
        if (currentPlaylist.isEmpty()) return;

        // Save current song to history before leaving
        songHistory.push(currentIndex);

        if (isShuffle) {
            // Pick a random index
            currentIndex = (int) (Math.random() * currentPlaylist.size());
        } else {
            // Go to next linearly
            currentIndex = (currentIndex + 1) % currentPlaylist.size();
        }
        playCurrentSong(context);
    }

    public void playPrevious(Context context) {
        if (currentPlaylist.isEmpty()) return;

        // 1. RESTART LOGIC: If played > 5 seconds, restart song
        if (mediaPlayer != null && mediaPlayer.getCurrentPosition() > 5000) {
            mediaPlayer.seekTo(0);
            return;
        }

        // 2. HISTORY LOGIC: Go back to the specific song previously played
        if (!songHistory.isEmpty()) {
            currentIndex = songHistory.pop();
        } else {
            // Fallback (Linear previous) if no history exists
            currentIndex = (currentIndex - 1 + currentPlaylist.size()) % currentPlaylist.size();
        }

        playCurrentSong(context);
    }

    private void notifyUI() {
        for (Runnable listener : listeners) {
            if (listener != null) listener.run();
        }
    }

    public boolean toggleShuffle() { isShuffle = !isShuffle; return isShuffle; }
    public boolean toggleRepeat() { isRepeat = !isRepeat; return isRepeat; }
    public boolean isShuffleEnabled() { return isShuffle; }
    public boolean isRepeatEnabled() { return isRepeat; }
    public boolean isPlaying() { return mediaPlayer != null && mediaPlayer.isPlaying(); }

    public Song getCurrentSong() {
        if (currentPlaylist.isEmpty() || currentIndex < 0 || currentIndex >= currentPlaylist.size()) return null;
        return currentPlaylist.get(currentIndex);
    }
    public MediaPlayer getMediaPlayer() { return mediaPlayer; }
}