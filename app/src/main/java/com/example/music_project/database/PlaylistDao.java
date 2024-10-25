package com.example.music_project.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import androidx.room.Delete;

import com.example.music_project.models.Playlist;

import java.util.List;

@Dao
public interface PlaylistDao {
    @Query("SELECT * FROM Playlist")
    List<Playlist> getAllPlaylists();

    @Query("SELECT * FROM Playlist WHERE Playlist_ID = :playlistId")
    Playlist getPlaylistById(int playlistId);

    @Query("SELECT * FROM Playlist WHERE User_ID = :userId")
    List<Playlist> getPlaylistsByUser(int userId);

    @Insert
    long insert(Playlist playlist);

    @Update
    void update(Playlist playlist);

    @Delete
    void delete(Playlist playlist);

    @Query("SELECT * FROM Playlist WHERE title LIKE '%' || :query || '%'")
    List<Playlist> searchPlaylists(String query);
}
