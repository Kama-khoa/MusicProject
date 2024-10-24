package com.example.music_project.controllers;

import com.example.music_project.database.AlbumSongDao;
import com.example.music_project.database.SongDao;
import com.example.music_project.models.Song;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import com.example.music_project.database.ArtistDao;
import com.example.music_project.database.AlbumDao;
import com.example.music_project.database.GenreDao;
import com.example.music_project.models.Artist;
import com.example.music_project.models.Album;
import com.example.music_project.models.Genre;

public class SongController {
    private SongDao songDao;
    private AlbumSongDao albumSongDao;
    private ArtistDao artistDao;
    private AlbumDao albumDao;
    private GenreDao genreDao;
    private ExecutorService executorService;

    public SongController(SongDao songDao, ArtistDao artistDao, AlbumDao albumDao, GenreDao genreDao) {
        this.songDao = songDao;
        this.artistDao = artistDao;
        this.albumDao = albumDao;
        this.genreDao = genreDao;
        this.executorService = Executors.newSingleThreadExecutor();
    }

    public SongController(SongDao songDao, AlbumSongDao albumSongDao) {
        this.songDao = songDao;
        this.albumSongDao = albumSongDao;
        this.executorService = Executors.newSingleThreadExecutor();
    }

    public void addSong(Song song, Callback<Void> callback) {
        executorService.execute(() -> {
            try {
                songDao.insert(song);
                callback.onSuccess(null);
            } catch (Exception e) {
                callback.onError("Không thể thêm bài hát: " + e.getMessage());
            }
        });
    }

    public void addSongWithArtist(Song song, int artistId, Callback<Void> callback) {
        // Gán artist_id vào bài hát
        song.setArtist_id(artistId);
        // Gọi hàm addSong để thêm bài hát vào cơ sở dữ liệu
        addSong(song, callback);
    }

    public void updateSong(Song song, Callback<Void> callback) {
        executorService.execute(() -> {
            try {
                songDao.update(song);
                callback.onSuccess(null);
            } catch (Exception e) {
                callback.onError("Không thể cập nhật bài hát: " + e.getMessage());
            }
        });
    }

    public void deleteSong(Song song, Callback<Void> callback) {
        executorService.execute(() -> {
            try {
                songDao.delete(song);
                callback.onSuccess(null);
            } catch (Exception e) {
                callback.onError("Không thể xóa bài hát: " + e.getMessage());
            }
        });
    }

    public void getAllSongs(Callback<List<Song>> callback) {
        executorService.execute(() -> {
            try {
                List<Song> songs = songDao.getAllSongsWithArtists();
                callback.onSuccess(songs);
            } catch (Exception e) {
                callback.onError("Không thể lấy danh sách bài hát: " + e.getMessage());
            }
        });
    }

    public void getSongById(int songId, Callback<Song> callback) {
        executorService.execute(() -> {
            try {
                Song song = songDao.getSongWithArtist(songId);
                if (song != null) {
                    callback.onSuccess(song);
                } else {
                    callback.onError("Không tìm thấy bài hát");
                }
            } catch (Exception e) {
                callback.onError("Không thể lấy thông tin bài hát: " + e.getMessage());
            }
        });
    }

    // Load songs for a specific album
    public void getSongsForAlbum(int albumId, OnSongsLoadedListener listener) {
        executorService.execute(() -> {
            List<Song> songs = albumSongDao.getSongsByAlbumId(albumId);
            if (songs != null && !songs.isEmpty()) {
                listener.onSongsLoaded(songs);
            } else {
                listener.onFailure("No songs found for the album.");
            }
        });
    }

    public void getAllArtists(Callback<List<Artist>> callback) {
        executorService.execute(() -> {
            try {
                List<Artist> artists = artistDao.getAllArtists(); // Lấy danh sách nghệ sĩ từ DAO
                callback.onSuccess(artists);
            } catch (Exception e) {
                callback.onError("Không thể lấy danh sách nghệ sĩ: " + e.getMessage());
            }
        });
    }

    public void getAllAlbums(Callback<List<Album>> callback) {
        executorService.execute(() -> {
            try {
                List<Album> albums = albumDao.getAllAlbums(); // Lấy danh sách album từ DAO
                callback.onSuccess(albums);
            } catch (Exception e) {
                callback.onError("Không thể lấy danh sách album: " + e.getMessage());
            }
        });
    }

    public void getAllGenres(Callback<List<Genre>> callback) {
        executorService.execute(() -> {
            try {
                List<Genre> genres = genreDao.getAllGenres(); // Lấy danh sách thể loại từ DAO
                callback.onSuccess(genres);
            } catch (Exception e) {
                callback.onError("Không thể lấy danh sách thể loại: " + e.getMessage());
            }
        });
    }

    public interface Callback<T> {
        void onSuccess(T result);

        void onError(String error);
    }

    public interface OnSongsLoadedListener {
        void onSongsLoaded(List<Song> songs);
        void onFailure(String error);
    }


    public interface OnArtistsLoadedListener {
        void onArtistsLoaded(List<Artist> artists);
        void onFailure(String message);
    }

    public interface OnAlbumsLoadedListener {
        void onAlbumsLoaded(List<Album> albums);
        void onFailure(String message);
    }

    public interface OnGenresLoadedListener {
        void onGenresLoaded(List<Genre> genres);
        void onFailure(String message);
    }
}