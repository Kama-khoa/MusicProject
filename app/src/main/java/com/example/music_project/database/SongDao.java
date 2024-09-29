package com.example.music_project.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import androidx.room.Delete;

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

    @Insert
    long insert(Song song);

    @Update
    void update(Song song);

    @Delete
    void delete(Song song);
}