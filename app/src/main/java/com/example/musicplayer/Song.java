package com.example.musicplayer;

public class Song {
    private int resId;
    private String title;
    private String artist;
    private String genre;

    public Song(int resId, String title, String artist, String genre) {
        this.resId = resId;
        this.title = title;
        this.artist = artist;
        this.genre = genre;
    }

    public int getResId() {
        return resId;
    }

    public String getTitle() {
        return title;
    }

    public String getArtist() {
        return artist;
    }

    public String getGenre() {
        return genre;
    }
}