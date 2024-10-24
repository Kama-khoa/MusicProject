package com.example.music_project.database;

import androidx.room.*;

import com.example.music_project.models.Album;
import com.example.music_project.models.Playlist;
import com.example.music_project.models.Song;

import java.util.List;

@Dao
public interface AlbumDao {
    @Insert
    long insert(Album album);

    @Query("SELECT * FROM Album")
    List<Album> getAllAlbums();

    @Query("SELECT * FROM Album WHERE album_id = :albumId")
    Album getAlbumById(int albumId);

    @Query("SELECT artist_name FROM Artist WHERE artist_id = (SELECT artist_id FROM Album WHERE album_id = :albumId)")
    String getArtistNameByAlbumId(int albumId);

    @Query("SELECT genre_name FROM Genre WHERE genre_id = (SELECT genre_id FROM Album WHERE album_id = :albumId)")
    String getGenreNameByAlbumId(int albumId);

    @Query("SELECT * FROM Album WHERE artist_id = :userId")
    List<Album> getAlbumsByUser(int userId);

    @Update
    void update(Album album);

    @Delete
    void delete(Album album);
}
