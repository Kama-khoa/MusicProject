package com.example.music_project.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.music_project.models.PlayHistory;
import com.example.music_project.models.Song;

import java.util.List;

@Dao
public interface PlayHistoryDao {
    @Query("SELECT * FROM PlayHistory WHERE UserID = :userId ORDER BY PlayedAt DESC LIMIT :limit")
    List<PlayHistory> getUserPlayHistory(int userId, int limit);

    @Query("SELECT s.* FROM Songs s INNER JOIN PlayHistory ph ON s.SongID = ph.SongID WHERE ph.UserID = :userId ORDER BY ph.PlayedAt DESC LIMIT :limit")
    List<Song> getRecentlyPlayedSongs(int userId, int limit);

    @Insert
    void insert(PlayHistory playHistory);
}