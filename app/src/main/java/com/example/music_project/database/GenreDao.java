package com.example.music_project.database;

import androidx.room.*;

import com.example.music_project.models.Genre;

import java.util.List;

@Dao
public interface GenreDao {
    @Insert
    void insert(Genre genre);

    @Query("SELECT * FROM genre")
    List<Genre> getAllGenres();

    @Query("SELECT * FROM Genre WHERE genre_id = :genreId")
    Genre getGenreById(int genreId);

    @Query("SELECT * FROM Genre")
    List<Genre> getAllGenres();

    @Update
    void update(Genre genre);

    @Delete
    void delete(Genre genre);
}
