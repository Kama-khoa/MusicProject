package com.example.music_project.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.music_project.models.PlayHistory;
import com.example.music_project.models.Playlist;
import com.example.music_project.models.Song;

import java.util.List;

@Dao
public interface PlayHistoryDao {
    @Insert
    void insert(PlayHistory playHistory);

    @Query("SELECT * FROM PlayHistory WHERE UserID = :userId ORDER BY PlayedAt DESC LIMIT :limit")
    List<PlayHistory> getUserPlayHistory(int userId, int limit);

    // Truy vấn để lấy danh sách bài hát gần đây từ PlayHistory
    @Query("SELECT * FROM Song WHERE song_id IN (SELECT songId FROM PlayHistory WHERE UserID = :userId ORDER BY PlayedAt DESC LIMIT 5)")
    LiveData<List<Song>> getRecentSongsFromHistory(long userId);

    // Truy vấn để lấy danh sách bài hát phổ biến dựa trên số lần phát
    @Query("SELECT Song.* FROM Song JOIN PlayHistory ON Song.song_id = PlayHistory.songId GROUP BY songId ORDER BY COUNT(songId) DESC LIMIT 5")
    LiveData<List<Song>> getPopularSongsFromHistory();

    @Update
    void update(PlayHistory playHistory);

    @Delete
    void delete(PlayHistory playHistory);
}