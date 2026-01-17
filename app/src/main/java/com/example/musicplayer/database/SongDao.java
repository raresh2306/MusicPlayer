package com.example.musicplayer.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface SongDao {
    @Insert
    void insertSong(SongEntity song);
    
    @Insert
    void insertAllSongs(List<SongEntity> songs);
    
    @Query("SELECT * FROM songs")
    List<SongEntity> getAllSongs();
    
    @Query("SELECT * FROM songs WHERE artist = :artist")
    List<SongEntity> getSongsByArtist(String artist);
    
    @Query("SELECT * FROM songs WHERE genre = :genre")
    List<SongEntity> getSongsByGenre(String genre);
    
    @Query("SELECT DISTINCT artist FROM songs")
    List<String> getUniqueArtists();
    
    @Query("SELECT DISTINCT genre FROM songs")
    List<String> getUniqueGenres();
    
    @Query("SELECT COUNT(*) FROM songs")
    int getSongCount();
}
