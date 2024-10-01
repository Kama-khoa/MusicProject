package com.example.music_project.api;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class SpotifyApiClient {
    private static final String BASE_URL = "https://api.spotify.com/";
    private static Retrofit retrofit = null;

    public static Retrofit getClient(String token) {
        if (retrofit == null) {
            // Tạo OkHttpClient với AuthInterceptor
            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(new AuthInterceptor(token)) // Thêm interceptor
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client) // Sử dụng OkHttpClient đã tạo
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }
}
