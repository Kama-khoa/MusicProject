package com.example.music_project.controllers;

import com.example.music_project.api.ApiService;
import com.example.music_project.api.SpotifyApiClient;
import com.example.music_project.api.responses.SongListResponse;
import com.example.music_project.database.SongDao;
import com.example.music_project.models.Song;

import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SongController {
    private ApiService apiService;
    private SongDao songDao;
    private ExecutorService executorService;

    public SongController(SongDao songDao) {
        this.apiService = SpotifyApiClient.getClient().create(ApiService.class);
        this.songDao = songDao;
        this.executorService = Executors.newSingleThreadExecutor();
    }

//    public void fetchAndSaveTracks(String accessToken) {
//        apiService.getSavedTracks("Bearer " + accessToken).enqueue(new Callback<SongListResponse>() {
//            @Override
//            public void onResponse(Call<SongListResponse> call, Response<SongListResponse> response) {
//                if (response.isSuccessful() && response.body() != null) {
//                    List<Song> songs = new ArrayList<>();
//                    for (SongListResponse.TrackItem item : response.body().getItems()) {
//                        songs.add(item.getTrack());
//                    }
//                    saveSongsToDatabase(songs);
//                }
//            }
//
//            @Override
//            public void onFailure(Call<SongListResponse> call, Throwable t) {
//                // Handle error
//            }
//        });
//    }

    private void saveSongsToDatabase(List<Song> songs) {
        executorService.execute(() -> songDao.insertAll(songs));
    }

    public List<Song> getLocalSongs() {
        return songDao.getAllSongs();
    }

    public Song getSong(String id) {
        return songDao.getItem(id);
    }

//    public void getRecentSongs(final Callback<List<Song>> callback) {
//        executorService.execute(() -> {
//            try {
//                // Assume we want to get the 10 most recent songs
//                List<Song> recentSongs = songDao.getRecentSongs(10);
//                callback.onSuccess(recentSongs);
//            } catch (Exception e) {
//                callback.onError(e.getMessage());
//            }
//        });
//    }

    public interface Callback<T> {
        void onSuccess(T result);
        void onError(String error);
    }
}