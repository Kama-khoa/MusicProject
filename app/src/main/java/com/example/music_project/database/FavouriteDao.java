package com.example.music_project.database;

import androidx.room.*;

import com.example.music_project.models.Favourite;

@Dao
public interface FavouriteDao {
    @Insert
    void insert(Favourite favourite);

    @Query("SELECT * FROM Favourite WHERE user_id = :userId AND song_id = :songId")
    Favourite getFavourite(int userId, int songId);

    @Delete
    void delete(Favourite favourite);
}
