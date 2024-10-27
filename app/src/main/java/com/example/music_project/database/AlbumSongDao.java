package com.example.music_project.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.music_project.models.Album;
import com.example.music_project.models.AlbumSong;
import com.example.music_project.models.PlaylistSong;
import com.example.music_project.models.Song;

import java.util.List;

@Dao
public interface AlbumSongDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(AlbumSong albumSong);

    @Query("DELETE FROM AlbumSong WHERE album_id = :albumId AND song_id = :songId")
    void delete(long albumId, long songId);

    @Query("SELECT * FROM AlbumSong WHERE album_id = :albumId")
    List<AlbumSong> getSongsForAlbum(int albumId);

    @Query("SELECT * FROM AlbumSong WHERE song_id = :songId")
    List<AlbumSong> getAlbumsForSong(int songId);

    @Query("SELECT * FROM AlbumSong WHERE album_id = :albumId AND song_id = :songId LIMIT 1")
    AlbumSong getAlbumSong(int albumId, int songId);
}
