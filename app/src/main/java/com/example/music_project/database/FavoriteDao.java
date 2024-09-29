package com.example.music_project.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Delete;

import com.example.music_project.models.Favorite;
import com.example.music_project.models.Song;

import java.util.List;

@Dao
public interface FavoriteDao {
    @Query("SELECT * FROM Favorites WHERE UserID = :userId")
    List<Favorite> getUserFavorites(int userId);

    @Query("SELECT s.* FROM Songs s INNER JOIN Favorites f ON s.SongID = f.SongID WHERE f.UserID = :userId")
    List<Song> getFavoriteSongs(int userId);

    @Insert
    void insert(Favorite favorite);

    @Delete
    void delete(Favorite favorite);
}