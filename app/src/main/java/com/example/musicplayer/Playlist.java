package com.example.musicplayer;

import java.util.ArrayList;
import java.util.List;

public class Playlist {
    private String id;
    private String name;
    private String userId;
    private List<String> songIds; // Lista de resId-uri sau cloudSongIds ale melodiilor
    private List<String> cloudSongIds; // Lista de ID-uri din Firestore pentru melodiile cloud
    private long createdAt;

    public Playlist() {
        this.songIds = new ArrayList<>();
        this.cloudSongIds = new ArrayList<>();
        this.createdAt = System.currentTimeMillis();
    }

    public Playlist(String name, String userId) {
        this.name = name;
        this.userId = userId;
        this.songIds = new ArrayList<>();
        this.cloudSongIds = new ArrayList<>();
        this.createdAt = System.currentTimeMillis();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public List<String> getSongIds() {
        return songIds;
    }

    public void setSongIds(List<String> songIds) {
        this.songIds = songIds;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public List<String> getCloudSongIds() {
        return cloudSongIds;
    }

    public void setCloudSongIds(List<String> cloudSongIds) {
        this.cloudSongIds = cloudSongIds;
    }

    public int getSongCount() {
        int localCount = songIds != null ? songIds.size() : 0;
        int cloudCount = cloudSongIds != null ? cloudSongIds.size() : 0;
        return localCount + cloudCount;
    }
}
