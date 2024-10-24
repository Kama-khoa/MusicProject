package com.example.music_project.database;

import androidx.room.*;

import com.example.music_project.models.Artist;

import java.util.List;

@Dao
public interface ArtistDao {
    @Insert
    long insert(Artist artist);

    @Query("SELECT * FROM Artist WHERE artist_id = :artistId")
    Artist getArtistById(int artistId);

    @Query("SELECT * FROM Artist")
    List<Artist> getAllArtists();

    @Update
    void update(Artist artist);

    @Delete
    void delete(Artist artist);
}
