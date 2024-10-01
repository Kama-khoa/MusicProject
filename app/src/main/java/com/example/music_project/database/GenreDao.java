package com.example.music_project.database;

import androidx.room.*;

import com.example.music_project.models.Genre;

@Dao
public interface GenreDao {
    @Insert
    void insert(Genre genre);

    @Query("SELECT * FROM Genre WHERE genre_id = :genreId")
    Genre getGenreById(int genreId);

    @Update
    void update(Genre genre);

    @Delete
    void delete(Genre genre);
}
