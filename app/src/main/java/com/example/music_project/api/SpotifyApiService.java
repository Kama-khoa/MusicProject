package com.example.music_project.api;

import com.example.music_project.models.Song;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface SpotifyApiService {
    @GET("v1/me/top/tracks")
    Call<List<Song>> getTopTracks(@Query("limit") int limit);
}