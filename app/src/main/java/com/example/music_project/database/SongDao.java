package com.example.music_project.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import androidx.room.Delete;
import androidx.room.OnConflictStrategy;
import androidx.room.Transaction;

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

    @Query("SELECT s.*, a.artist_name as artistName FROM Song s " +
            "JOIN Artist a ON s.artist_id = a.artist_id " +
            "WHERE s.title LIKE '%' || :query || '%' OR a.artist_name LIKE '%' || :query || '%'")
    List<Song> searchSongs(String query);

    @Query("SELECT s.*, a.artist_name as artistName FROM Song s " +
            "JOIN Artist a ON s.artist_id = a.artist_id " +
            "WHERE s.song_id NOT IN (SELECT PlaylistSong.song_id FROM PlaylistSong WHERE PlaylistSong.playlist_id = :playlistId)")
    List<Song> getAvailableSongs(int playlistId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void addSongToPlaylist(PlaylistSong playlistSong);

    // Additional queries for artist name
    @Query("SELECT a.artist_name FROM Artist a WHERE a.artist_id = :artistId")
    String getArtistNameById(int artistId);

    @Transaction
    @Query("SELECT s.*, a.artist_name as artistName FROM Song s " +
            "JOIN Artist a ON s.artist_id = a.artist_id " +
            "ORDER BY s.title")
    List<Song> getAllSongsWithArtistsSorted();
}