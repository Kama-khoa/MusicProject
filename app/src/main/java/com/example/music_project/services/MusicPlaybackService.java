package com.example.music_project.services;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.example.music_project.models.Song;

public class MusicPlaybackService extends Service {
    private static final String TAG = "MusicPlaybackService";
    private final IBinder binder = new MusicBinder();
    private MediaPlayer mediaPlayer;
    private boolean isPrepared = false;
    private Song currentSong = null;

    public class MusicBinder extends Binder {
        public MusicPlaybackService getService() {
            return MusicPlaybackService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public void playSong(Song song) {
        if (song == null || song.getFile_path() == null) {
            Log.e(TAG, "Invalid song or file path");
            Toast.makeText(this, "Cannot play song: Invalid song data", Toast.LENGTH_SHORT).show();
            return;
        }

        currentSong = song;
        isPrepared = false;

        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }

        try {
            mediaPlayer = new MediaPlayer();
            Uri songUri = Uri.parse(song.getFile_path());

            mediaPlayer.setDataSource(getApplicationContext(), songUri);

            mediaPlayer.setOnPreparedListener(mp -> {
                isPrepared = true;
                mp.start();
                Log.d(TAG, "Now playing: " + song.getTitle() + " by " + song.getArtistName());
            });

            mediaPlayer.setOnErrorListener((mp, what, extra) -> {
                Log.e(TAG, "MediaPlayer error: " + what + ", " + extra);
                Toast.makeText(getApplicationContext(),
                        "Error playing music: " + what,
                        Toast.LENGTH_SHORT).show();
                isPrepared = false;
                return false;
            });

            mediaPlayer.setOnCompletionListener(mp -> {
                Log.d(TAG, "Song completed: " + song.getTitle());
                isPrepared = false;
            });

            mediaPlayer.prepareAsync();

        } catch (Exception e) {
            Log.e(TAG, "Error playing song: " + e.getMessage());
            Toast.makeText(this,
                    "Cannot play song: " + e.getMessage(),
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

    public Song getCurrentSong() {
        return currentSong;
    }

    public boolean isPrepared() {
        return isPrepared;
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
