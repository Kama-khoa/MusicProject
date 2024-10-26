package com.example.music_project.services;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;

public class MusicPlaybackService extends Service {
    private static final String TAG = "MusicPlaybackService";
    private final IBinder binder = new MusicBinder();
    private MediaPlayer mediaPlayer;
    private boolean isPrepared = false;
    private String currentResourceId = null;

    public class MusicBinder extends Binder {
        public MusicPlaybackService getService() {
            return MusicPlaybackService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public void playSong(String resourceId) {
        if (resourceId == null) {
            Log.e(TAG, "Resource ID is null");
            Toast.makeText(this, "Không thể phát bài hát: Resource ID không hợp lệ", Toast.LENGTH_SHORT).show();
            return;
        }

        // Nếu đang phát bài hát cũ, dừng và giải phóng
        if (mediaPlayer != null) {
            mediaPlayer.release();
        }

        try {
            // Khởi tạo MediaPlayer mới
            mediaPlayer = new MediaPlayer();
            isPrepared = false;
            currentResourceId = resourceId;

            // Tạo Uri từ resource ID
            Uri songUri = Uri.parse("android.resource://" + getPackageName() + "/raw/" + resourceId);

            // Set data source với Uri
            mediaPlayer.setDataSource(getApplicationContext(), songUri);

            // Set các listener
            mediaPlayer.setOnPreparedListener(mp -> {
                isPrepared = true;
                mp.start();
                Log.d(TAG, "Media player prepared and started");
            });

            mediaPlayer.setOnErrorListener((mp, what, extra) -> {
                Log.e(TAG, "MediaPlayer error: " + what + ", " + extra);
                Toast.makeText(getApplicationContext(),
                        "Lỗi phát nhạc: " + what,
                        Toast.LENGTH_SHORT).show();
                isPrepared = false;
                return false;
            });

            mediaPlayer.setOnCompletionListener(mp -> {
                Log.d(TAG, "Song completed");
                isPrepared = false;
            });

            // Chuẩn bị và phát nhạc
            mediaPlayer.prepareAsync();

        } catch (IOException e) {
            Log.e(TAG, "Error playing song: " + e.getMessage());
            Toast.makeText(this,
                    "Không thể phát bài hát: " + e.getMessage(),
                    Toast.LENGTH_SHORT).show();
            isPrepared = false;
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "Invalid resource URI: " + e.getMessage());
            Toast.makeText(this,
                    "Resource không hợp lệ: " + e.getMessage(),
                    Toast.LENGTH_SHORT).show();
            isPrepared = false;
        } catch (Exception e) {
            Log.e(TAG, "Unexpected error: " + e.getMessage());
            Toast.makeText(this,
                    "Lỗi không xác định: " + e.getMessage(),
                    Toast.LENGTH_SHORT).show();
            isPrepared = false;
        }
    }

    public boolean isPlaying() {
        return mediaPlayer != null && mediaPlayer.isPlaying();
    }

    public void pauseSong() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
        }
    }

    public void resumeSong() {
        if (mediaPlayer != null && !mediaPlayer.isPlaying() && isPrepared) {
            mediaPlayer.start();
        }
    }

    public int getCurrentPosition() {
        if (mediaPlayer != null && isPrepared) {
            return mediaPlayer.getCurrentPosition();
        }
        return 0;
    }

    public int getDuration() {
        if (mediaPlayer != null && isPrepared) {
            return mediaPlayer.getDuration();
        }
        return 0;
    }

    public void seekTo(int position) {
        if (mediaPlayer != null && isPrepared) {
            mediaPlayer.seekTo(position);
        }
    }

    public void stopSong() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
            isPrepared = false;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
            isPrepared = false;
        }
    }
}