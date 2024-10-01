package com.example.music_project.database;

import androidx.room.*;

import com.example.music_project.models.History;

@Dao
public interface HistoryDao {
    @Insert
    void insert(History history);

    @Query("SELECT * FROM History WHERE user_id = :userId AND artist_id = :artistId")
    History getHistory(int userId, int artistId);

    @Update
    void update(History history);

    @Delete
    void delete(History history);
}