package com.example.music_project.database;

import androidx.room.*;

import com.example.music_project.models.Artist;

@Dao
public interface ArtistDao {
    @Insert
    void insert(Artist artist);

    @Query("SELECT * FROM Artist WHERE artist_id = :artistId")
    Artist getArtistById(int artistId);

    @Update
    void update(Artist artist);

    @Delete
    void delete(Artist artist);
}
