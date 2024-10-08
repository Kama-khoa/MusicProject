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
    @Query("SELECT * FROM PlaylistSong WHERE Playlist_ID = :playlistId")
    List<PlaylistSong> getPlaylistSongs(int playlistId);

    @Query("SELECT s.* FROM Song s INNER JOIN PlaylistSong ps ON s.Song_ID = ps.Song_ID WHERE ps.Playlist_ID = :playlistId")
    List<Song> getSongsInPlaylist(int playlistId);

    @Insert
    void insert(PlaylistSong playlistSong);

    @Delete
    void delete(PlaylistSong playlistSong);

    @Query("SELECT * FROM Song INNER JOIN PlaylistSong ON Song.song_id = PlaylistSong.song_id WHERE PlaylistSong.playlist_id = :playlistId")
    List<Song> getSongsByPlaylistId(int playlistId);
}
