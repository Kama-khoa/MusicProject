package com.example.music_project.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import androidx.room.Delete;
import androidx.room.OnConflictStrategy;

import com.example.music_project.models.Song;

import java.util.List;

@Dao
public interface SongDao {
    @Query("SELECT * FROM Songs")
    List<Song> getAllSongs();

    @Query("SELECT * FROM Songs WHERE SongID = :songId")
    Song getSongById(int songId);

    @Query("SELECT * FROM Songs WHERE Artist = :artist")
    List<Song> getSongsByArtist(String artist);

    @Query("SELECT * FROM songs ") // Lấy 10 bài hát gần đây ORDER BY created_at DESC LIMIT 10
    List<Song> getRecentSongs();

    @Query("SELECT * FROM Songs") // Lấy 10 bài hát phổ biến  ORDER BY play_count DESC LIMIT 10
    List<Song> getPopularSongs();

    @Insert
    long insert(Song song);

    @Update
    void update(Song song);

    @Delete
    void delete(Song song);

    @Query("SELECT * FROM songs WHERE SongID = :id")
    Song getItem(String id);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<Song> songs);
}