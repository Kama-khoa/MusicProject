package com.example.music_project.database;


import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Delete;

import com.example.music_project.models.PlaylistSong;
import com.example.music_project.models.Song;

import java.util.List;

@Dao
public interface PlaylistSongDao {
    @Query("SELECT * FROM PlaylistSongs WHERE PlaylistID = :playlistId")
    List<PlaylistSong> getPlaylistSongs(int playlistId);

    @Query("SELECT s.* FROM Songs s INNER JOIN PlaylistSongs ps ON s.SongID = ps.SongID WHERE ps.PlaylistID = :playlistId")
    List<Song> getSongsInPlaylist(int playlistId);

    @Insert
    void insert(PlaylistSong playlistSong);

    @Delete
    void delete(PlaylistSong playlistSong);
}
