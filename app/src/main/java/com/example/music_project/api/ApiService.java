package com.example.music_project.api;

import com.example.music_project.api.responses.LoginResponse;
import com.example.music_project.api.responses.SongListResponse;
import retrofit2.Call;
import retrofit2.http.*;

public interface ApiService {
    @POST("api/token")
    @FormUrlEncoded
    Call<LoginResponse> getAccessToken(
            @Field("grant_type") String grantType,
            @Field("client_id") String clientId,
            @Field("client_secret") String clientSecret
    );

    @GET("v1/me/tracks")
    Call<SongListResponse> getSavedTracks(@Header("Authorization") String accessToken);

    @GET("v1/search")
    Call<SongListResponse> searchTracks(
            @Header("Authorization") String accessToken,
            @Query("q") String query,
            @Query("type") String type
    );

    @GET("v1/tracks/{id}")
    Call<SongListResponse> getTrack(
            @Header("Authorization") String accessToken,
            @Path("id") String trackId
    );
    @GET("v1/me/top/tracks")
    Call<SongListResponse> getTopTracks(@Header("Authorization") String accessToken,
                                        @Query("limit") int limit);
}