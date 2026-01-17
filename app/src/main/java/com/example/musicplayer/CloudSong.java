package com.example.musicplayer;

/**
 * Reprezintă metadata unei melodii stocate în cloud (Firestore)
 * Melodia în sine rămâne locală pe dispozitiv
 */
public class CloudSong {
    private String id;
    private String title;
    private String artist;
    private String genre;
    private String userId; // Utilizatorul care a adăugat melodia
    private String localPath; // Calea locală sau resId ca string
    private long createdAt;
    private boolean isFromResources; // true dacă e din res/raw, false dacă e fișier local

    public CloudSong() {
        this.createdAt = System.currentTimeMillis();
    }

    public CloudSong(String title, String artist, String genre, String userId, String localPath, boolean isFromResources) {
        this.title = title;
        this.artist = artist;
        this.genre = genre;
        this.userId = userId;
        this.localPath = localPath;
        this.isFromResources = isFromResources;
        this.createdAt = System.currentTimeMillis();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getLocalPath() {
        return localPath;
    }

    public void setLocalPath(String localPath) {
        this.localPath = localPath;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isFromResources() {
        return isFromResources;
    }

    public void setFromResources(boolean fromResources) {
        isFromResources = fromResources;
    }
}
