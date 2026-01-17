package com.example.musicplayer.database;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "songs")
public class SongEntity {
    @PrimaryKey(autoGenerate = true)
    private long id;
    
    private int resId; // 0 dacă e din cloud
    private String cloudUrl; // URL pentru melodiile din cloud
    private String title;
    private String artist;
    private String genre;
    private boolean isCloudSong; // true dacă e din cloud
    private String coverImageUrl; // URL pentru poza de cover (pentru melodiile cloud)
    
    public SongEntity() {
    }
    
    @Ignore
    public SongEntity(int resId, String title, String artist, String genre) {
        this.resId = resId;
        this.title = title;
        this.artist = artist;
        this.genre = genre;
        this.isCloudSong = false;
        this.cloudUrl = null;
    }
    
    @Ignore
    public SongEntity(String cloudUrl, String title, String artist, String genre) {
        this.cloudUrl = cloudUrl;
        this.title = title;
        this.artist = artist;
        this.genre = genre;
        this.isCloudSong = true;
        this.resId = 0;
        this.coverImageUrl = null;
    }
    
    @Ignore
    public SongEntity(String cloudUrl, String title, String artist, String genre, String coverImageUrl) {
        this.cloudUrl = cloudUrl;
        this.title = title;
        this.artist = artist;
        this.genre = genre;
        this.isCloudSong = true;
        this.resId = 0;
        this.coverImageUrl = coverImageUrl;
    }
    
    public long getId() {
        return id;
    }
    
    public void setId(long id) {
        this.id = id;
    }
    
    public int getResId() {
        return resId;
    }
    
    public void setResId(int resId) {
        this.resId = resId;
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

    public String getCloudUrl() {
        return cloudUrl;
    }

    public void setCloudUrl(String cloudUrl) {
        this.cloudUrl = cloudUrl;
    }

    public boolean isCloudSong() {
        return isCloudSong;
    }

    public void setCloudSong(boolean cloudSong) {
        isCloudSong = cloudSong;
    }

    public String getCoverImageUrl() {
        return coverImageUrl;
    }

    public void setCoverImageUrl(String coverImageUrl) {
        this.coverImageUrl = coverImageUrl;
    }
}
