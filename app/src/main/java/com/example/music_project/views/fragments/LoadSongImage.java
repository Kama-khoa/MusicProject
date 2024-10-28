package com.example.music_project.views.fragments;

import android.util.Log;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.example.music_project.R;
import com.example.music_project.database.SongImageDao;
import com.example.music_project.models.SongImage;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class LoadSongImage {
    private ImageView imageView;
    private SongImageDao songImageDao;
    private Executor executor = Executors.newSingleThreadExecutor();

    public LoadSongImage(ImageView imageView, SongImageDao songImageDao) {
        this.imageView = imageView;
        this.songImageDao = songImageDao;
    }

    public void load(int songId) {
        executor.execute(() -> {
            SongImage songImage = songImageDao.getSongImageById(songId);
            if (songImage != null) {
                // Chuyển về UI thread để cập nhật ImageView
                imageView.post(() -> {
                    Glide.with(imageView.getContext())
                            .load(songImage.getImagePath())
                            .placeholder(R.drawable.default_song_cover)
                            .error(R.drawable.default_song_cover)
                            .override(54, 54)
                            .centerCrop()
                            .into(imageView);
                });
            }
        });
    }
}