package com.example.musicplayer;

import java.io.Serializable;

public class Song implements Serializable {
    private int resId;
    private String cloudUrl;
    private String title;
    private String artist;
    private String genre;
    private boolean isCloudSong;
    private String coverImageUrl;

    public Song(int resId, String title, String artist, String genre) {
        this.resId = resId;
        this.title = title;
        this.artist = artist;
        this.genre = genre;
        this.isCloudSong = false;
        this.cloudUrl = null;
        this.coverImageUrl = null;
    }

    public Song(String cloudUrl, String title, String artist, String genre) {
        this.cloudUrl = cloudUrl;
        this.title = title;
        this.artist = artist;
        this.genre = genre;
        this.isCloudSong = true;
        this.resId = 0;
        this.coverImageUrl = null;
    }

    public Song(String cloudUrl, String title, String artist, String genre, String coverImageUrl) {
        this.cloudUrl = cloudUrl;
        this.title = title;
        this.artist = artist;
        this.genre = genre;
        this.isCloudSong = true;
        this.resId = 0;
        this.coverImageUrl = coverImageUrl;
    }

    public String getStringId() {
        if (isCloudSong) {
            return cloudUrl;
        } else {
            return artist + "###" + title;
        }
    }

    public int getResId() { return resId; }
    public void setResId(int resId) { this.resId = resId; }
    public String getCloudUrl() { return cloudUrl; }
    public void setCloudUrl(String cloudUrl) { this.cloudUrl = cloudUrl; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getArtist() { return artist; }
    public void setArtist(String artist) { this.artist = artist; }
    public String getGenre() { return genre; }
    public void setGenre(String genre) { this.genre = genre; }
    public boolean isCloudSong() { return isCloudSong; }
    public void setCloudSong(boolean cloudSong) { isCloudSong = cloudSong; }
    public String getCoverImageUrl() { return coverImageUrl; }
    public void setCoverImageUrl(String coverImageUrl) { this.coverImageUrl = coverImageUrl; }
}