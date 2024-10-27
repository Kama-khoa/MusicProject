package com.example.music_project.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;
import androidx.room.Delete;
import androidx.room.OnConflictStrategy;

import com.example.music_project.models.AlbumSong;
import com.example.music_project.models.PlaylistSong;
import com.example.music_project.models.Song;

import java.util.List;

@Dao
public interface SongDao {
    @Query("SELECT * FROM Song")
    List<Song> getAllSongs();

    @Query("SELECT * FROM Song WHERE song_id = :songId")
    Song getSongById(int songId);

    @Query("SELECT s.*, a.artist_name as artistName FROM Song s " +
            "JOIN Artist a ON s.artist_id = a.artist_id " +
            "WHERE s.artist_id = :artist")
    List<Song> getSongsByArtist(int artist);

    @Query("SELECT s.*, a.artist_name as artistName FROM Song s " +
            "JOIN Artist a ON s.artist_id = a.artist_id " +
            "ORDER BY s.release_date DESC LIMIT 10")
    List<Song> getRecentSongs();

    @Query("SELECT * FROM Song") // Lấy 10 bài hát phổ biến  ORDER BY play_count DESC LIMIT 10
    List<Song> getPopularSongs();

    @Insert
    long insert(Song song);

    @Update
    void update(Song song);

    @Delete
    void delete(Song song);

    @Query("SELECT s.*, a.artist_name as artistName FROM Song s " +
            "JOIN Artist a ON s.artist_id = a.artist_id " +
            "WHERE s.song_id = :id")
    Song getItem(String id);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<Song> songs);

    @Query("SELECT s.*, a.artist_name as artistName FROM Song s " +
            "JOIN Artist a ON s.artist_id = a.artist_id")
    List<Song> getAllSongsWithArtists();

    @Query("SELECT s.*, a.artist_name as artistName FROM Song s " +
            "JOIN Artist a ON s.artist_id = a.artist_id " +
            "WHERE s.song_id = :songId")
    Song getSongWithArtist(int songId);

    @Query("SELECT s.*, a.artist_name as artistName FROM Song s " +
            "JOIN Artist a ON s.artist_id = a.artist_id " +
            "WHERE s.song_id IN (SELECT song_id FROM PlaylistSong WHERE playlist_id = :playlistId)")
    List<Song> getSongsInPlaylist(int playlistId);

    @Query("SELECT * FROM Song WHERE song_id IN(SELECT song_id FROM AlbumSong WHERE AlbumSong.album_id = :albumId)")
    List<Song> getSongsByAlbumId(int albumId);

    @Query("SELECT s.*, a.artist_name as artistName FROM Song s " +
            "JOIN Artist a ON s.artist_id = a.artist_id " +
            "WHERE s.title LIKE '%' || :query || '%' OR a.artist_name LIKE '%' || :query || '%'")
    List<Song> searchSongs(String query);

    @Query("SELECT s.*, a.artist_name as artistName FROM Song s " +
            "JOIN Artist a ON s.artist_id = a.artist_id " +
            "WHERE s.song_id NOT IN (SELECT PlaylistSong.song_id FROM PlaylistSong WHERE PlaylistSong.playlist_id = :playlistId)")
    List<Song> getAvailableSongs(int playlistId);

    @Query("SELECT * FROM Song WHERE Song.song_id NOT IN (SELECT AlbumSong.song_id FROM AlbumSong WHERE AlbumSong.album_id = :albumId)")
    List<Song> getAvailableAlbumSongs(int albumId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void addSongToAlbum(AlbumSong albumSong);

    // Additional queries for artist name
    @Query("SELECT a.artist_name FROM Artist a WHERE a.artist_id = :artistId")
    String getArtistNameById(int artistId);

    @Transaction
    @Query("SELECT s.*, a.artist_name as artistName FROM Song s " +
            "JOIN Artist a ON s.artist_id = a.artist_id " +
            "ORDER BY s.title")
    List<Song> getAllSongsWithArtistsSorted();


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void addSongToPlaylist(PlaylistSong playlistSong);


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertSongWithCustomId(Song song);


    // Phương thức lấy số lượng bài hát trong bảng
    @Query("SELECT COUNT(*) FROM Song")
    int getCount();


}