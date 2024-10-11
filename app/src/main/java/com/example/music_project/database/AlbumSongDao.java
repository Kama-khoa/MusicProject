package com.example.music_project.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.music_project.models.Album;
import com.example.music_project.models.AlbumSong;
import com.example.music_project.models.Song;

import java.util.List;

@Dao
public interface AlbumSongDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(AlbumSong albumSong);

    @Query("DELETE FROM album_song WHERE album_id = :albumId AND song_id = :songId")
    void delete(long albumId, long songId);

    @Query("SELECT * FROM album_song WHERE album_id = :albumId")
    List<AlbumSong> getSongsForAlbum(long albumId);

    @Query("SELECT * FROM album_song WHERE song_id = :songId")
    List<AlbumSong> getAlbumsForSong(long songId);

    @Query("SELECT Song.* FROM Song INNER JOIN Album ON Song.album_id = Album.album_id WHERE Album.album_id = :albumId")
    List<Song> getSongsByAlbumId(int albumId);
}
