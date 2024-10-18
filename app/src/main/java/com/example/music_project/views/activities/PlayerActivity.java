package com.example.music_project.views.activities;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.music_project.R;
import com.example.music_project.controllers.SongController;
import com.example.music_project.database.AppDatabase;
import com.example.music_project.models.Song;
import com.example.music_project.services.MusicPlaybackService;
import com.example.music_project.views.fragments.PlayerControlsFragment;

public class PlayerActivity extends AppCompatActivity {

    private MusicPlaybackService musicService;
    private boolean isBound = false;
    private SongController songController;
    private ImageView albumCoverImageView;
    private TextView trackInfoTextView;
    private SeekBar playbackSeekBar;
    private ImageButton playPauseButton;
    private ImageButton previousButton;
    private ImageButton nextButton;
    private PlayerControlsFragment playerControlFragment;
    private Handler handler = new Handler();

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicPlaybackService.MusicBinder binder = (MusicPlaybackService.MusicBinder) service;
            musicService = binder.getService();
            isBound = true;
            onConnected();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            musicService = null;
            isBound = false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        songController = new SongController(AppDatabase.getInstance(this).songDao());

        albumCoverImageView = findViewById(R.id.albumCoverImageView);
        trackInfoTextView = findViewById(R.id.trackInfoTextView);
        playbackSeekBar = findViewById(R.id.playbackSeekBar);
        playPauseButton = findViewById(R.id.playPauseButton);
        previousButton = findViewById(R.id.previousButton);
        nextButton = findViewById(R.id.nextButton);

        Intent intent = new Intent(this, MusicPlaybackService.class);
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);

        playerControlFragment = PlayerControlsFragment.newInstance();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.player_control_container, playerControlFragment)
                .commit();
    }

    private void onConnected() {
        playPauseButton.setOnClickListener(v -> togglePlayPause());
        previousButton.setOnClickListener(v -> previousSong());
        nextButton.setOnClickListener(v -> nextSong());
        playbackSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && isBound) {
                    musicService.seekTo(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        updateTrackInfo();
        startPlaybackPositionTimer();
    }

    private void togglePlayPause() {
        if (isBound) {
            if (musicService.isPlaying()) {
                musicService.pause();
                playPauseButton.setImageResource(android.R.drawable.ic_media_play);
            } else {
                musicService.resume();
                playPauseButton.setImageResource(android.R.drawable.ic_media_pause);
            }
        }
    }

    private void previousSong() {
        // Implement logic to play the previous song
        // This might involve getting the previous song from your playlist or database
    }

    private void nextSong() {
        // Implement logic to play the next song
        // This might involve getting the next song from your playlist or database
    }

    private void updateTrackInfo() {
        if (isBound) {
            Song currentSong = musicService.getCurrentSong();
            if (currentSong != null) {
                String trackInfo = currentSong.getTitle() + " - " + currentSong.getArtistName();
                trackInfoTextView.setText(trackInfo);
                // Update album cover image
                // albumCoverImageView.setImageBitmap(...);
            }
        }
    }

    private void startPlaybackPositionTimer() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (isBound && musicService.isPlaying()) {
                    int currentPosition = musicService.getCurrentPosition();
                    playbackSeekBar.setProgress(currentPosition);
                }
                handler.postDelayed(this, 1000);
            }
        }, 1000);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isBound) {
            unbindService(serviceConnection);
            isBound = false;
        }
        handler.removeCallbacksAndMessages(null);
    }
}