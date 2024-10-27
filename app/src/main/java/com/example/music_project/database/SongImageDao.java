package com.example.music_project.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.music_project.models.SongImage;

import java.util.List;

@Dao
public interface SongImageDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(SongImage songImage);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<SongImage> songImages);

    @Update
    void update(SongImage songImage);

    @Delete
    void delete(SongImage songImage);

    @Query("SELECT * FROM SongImage WHERE song_id = :songId LIMIT 1")
    SongImage getSongImageById(int songId);

}