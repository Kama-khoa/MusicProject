package com.example.music_project.controllers;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import com.example.music_project.database.AppDatabase;
import com.example.music_project.models.Album;
import com.example.music_project.models.Artist;
import com.example.music_project.models.Song;

import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ArtistController {
    private AppDatabase database;
    private ExecutorService executorService;
    private Context context;

    public ArtistController(Context context) {
        this.context = context;
        database = AppDatabase.getInstance(context);
        executorService = Executors.newSingleThreadExecutor();
    }

    // Method to create an artist
    public void createArtist(Artist artist, final OnArtistCreatedListener listener) {
        executorService.execute(() -> {
            long artistId = database.artistDao().insert(artist);
            if (artistId > 0) {
                new Handler(Looper.getMainLooper()).post(() -> {
                    listener.onSuccess();
                    Toast.makeText(context, "Ca sĩ tạo thành công!", Toast.LENGTH_SHORT).show();
                });
            } else {
                new Handler(Looper.getMainLooper()).post(() -> listener.onFailure("Không thể tạo ca sĩ!"));
            }
        });
    }

    // Method to load all artists
    public void getArtists(final OnArtistsLoadedListener listener) {
        executorService.execute(() -> {
            List<Artist> artists = database.artistDao().getAllArtists();
            if (artists != null && !artists.isEmpty()) {
                new Handler(Looper.getMainLooper()).post(() -> listener.onArtistLoaded(artists));
            } else {
                new Handler(Looper.getMainLooper()).post(() -> listener.onFailure("Không có danh sách phát nào"));
            }
        });
    }

    // Method to load albums of a specific artist
    public void getArtistAlbums(int artistId, final OnAlbumsLoadedListener listener) {
        executorService.execute(() -> {
            List<Album> albums = database.albumDao().getAlbumsByUser(artistId);
            if (albums != null && !albums.isEmpty()) {
                new Handler(Looper.getMainLooper()).post(() -> listener.onAlbumsLoaded(albums));
            } else {
                new Handler(Looper.getMainLooper()).post(() -> listener.onFailure("Không có album nào"));
            }
        });
    }

    // Method to load songs of a specific artist
    public void getArtistSongs(int artistId, final OnSongsLoadedListener listener) {
        executorService.execute(() -> {
            List<Song> songs = database.songDao().getSongsByArtist(artistId);
            if (songs != null && !songs.isEmpty()) {
                new Handler(Looper.getMainLooper()).post(() -> listener.onSongsLoaded(songs));
            } else {
                new Handler(Looper.getMainLooper()).post(() -> listener.onFailure("Không có bài hát nào"));
            }
        });
    }

    public void getArtistById(int artistId, OnArtistLoadedListener listener) {
        executorService.execute(() -> {
            Artist artist = database.artistDao().getArtistById(artistId);
            if (artist != null) {
                listener.onArtistLoaded(artist);
            } else {
                listener.onFailure("Không tìm thấy nghệ sĩ với ID: " + artistId);
            }
        });
    }

    // Method to update an artist's information
    public void updateArtist(int artistId, String name, Date dob, String bio, String avatarPath, OnArtistUpdatedListener listener) {
        executorService.execute(() -> {
            Artist artist = database.artistDao().getArtistById(artistId); // Tìm ca sĩ theo ID
            if (artist != null) {
                artist.setArtist_name(name);
                artist.setDate_of_birth(dob);
                artist.setBio(bio);
                artist.setAvatar(avatarPath);
                database.artistDao().update(artist); // Cập nhật ca sĩ trong cơ sở dữ liệu

                // Gọi lại listener khi cập nhật thành công
                new Handler(Looper.getMainLooper()).post(listener::onSuccess);
            } else {
                new Handler(Looper.getMainLooper()).post(() -> listener.onFailure("Không tìm thấy ca sĩ"));
            }
        });
    }


    // Method to delete an artist
    public void deleteArtist(int artistId, OnArtistDeletedListener listener) {
        executorService.execute(() -> {
            Artist artist = database.artistDao().getArtistById(artistId); // Tìm ca sĩ theo ID
            if (artist != null) {
                database.artistDao().delete(artist); // Xóa ca sĩ trong cơ sở dữ liệu

                // Gọi lại listener khi xóa thành công
                new Handler(Looper.getMainLooper()).post(listener::onSuccess);
            } else {
                new Handler(Looper.getMainLooper()).post(() -> listener.onFailure("Không tìm thấy ca sĩ"));
            }
        });
    }

    // Listener interfaces for update and delete operations
    public interface OnArtistUpdatedListener {
        void onSuccess();
        void onFailure(String error);
    }

    public interface OnArtistDeletedListener {
        void onSuccess();
        void onFailure(String error);
    }

    // Listener interfaces
    public interface OnArtistCreatedListener {
        void onSuccess();
        void onFailure(String error);
    }

    public interface OnArtistLoadedListener {
        void onArtistLoaded(Artist artist);
        void onFailure(String error);
    }

    public interface OnArtistsLoadedListener {
        void onArtistLoaded(List<Artist> artists);
        void onFailure(String error);
    }

    public interface OnAlbumsLoadedListener {
        void onAlbumsLoaded(List<Album> albums);
        void onFailure(String error);
    }

    public interface OnSongsLoadedListener {
        void onSongsLoaded(List<Song> songs);
        void onFailure(String error);
    }
}
