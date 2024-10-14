package com.example.music_project.services;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import androidx.annotation.Nullable;
import com.example.music_project.models.Song;
import java.io.IOException;

public class MusicPlaybackService extends Service {
    private final IBinder binder = new MusicBinder();
    private MediaPlayer mediaPlayer;
    private Song currentSong;

    public class MusicBinder extends Binder {
        public MusicPlaybackService getService() {
            return MusicPlaybackService.this;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mediaPlayer = new MediaPlayer();
    }

    public void playSong(Song song) {
        if (currentSong != null && currentSong.getSong_id() == song.getSong_id() && mediaPlayer.isPlaying()) {
            return; // Nếu bài hát đang phát là bài hát được yêu cầu, không làm gì cả
        }

        currentSong = song;
        try {
            mediaPlayer.reset();
            if (song.getFile_path().startsWith("android.resource")) {
                mediaPlayer.setDataSource(getApplicationContext(), Uri.parse(song.getFile_path()));
            } else {
                mediaPlayer.setDataSource(song.getFile_path());
            }
            mediaPlayer.prepare();
            mediaPlayer.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void pause() {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
        }
    }

    public void resume() {
        if (!mediaPlayer.isPlaying()) {
            mediaPlayer.start();
        }
    }

    public void stop() {
        mediaPlayer.stop();
        mediaPlayer.reset();
        currentSong = null;
    }

    public boolean isPlaying() {
        return mediaPlayer.isPlaying();
    }

    public int getCurrentPosition() {
        return mediaPlayer.getCurrentPosition();
    }

    public int getDuration() {
        return mediaPlayer.getDuration();
    }

    public void seekTo(int position) {
        mediaPlayer.seekTo(position);
    }

    public Song getCurrentSong() {
        return currentSong;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    // Add method to play next song
    public void playNext() {
        // Logic to play next song in your playlist
    }

    // Add method to play previous song
    public void playPrevious() {
        // Logic to play previous song in your playlist
    }
}
