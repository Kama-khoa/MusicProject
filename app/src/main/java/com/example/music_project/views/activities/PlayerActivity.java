package com.example.music_project.views.activities;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.music_project.R;
import com.example.music_project.database.AppDatabase;
import com.example.music_project.models.Song;
import com.example.music_project.services.MusicPlaybackService;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class PlayerActivity extends AppCompatActivity {
    private static final String TAG = "PlayerActivity";

    // Service related
    private MusicPlaybackService musicService;
    private boolean isBound = false;

    // UI Components
    private ImageButton playPauseButton;
    private ImageButton nextButton;
    private ImageButton previousButton;
    private SeekBar playbackSeekBar;
    private TextView currentTimeTextView;
    private TextView totalTimeTextView;
    private TextView trackInfoTextView;
    private ImageView albumCoverImageView;

    // Database and playlist
    private AppDatabase database;
    private List<Song> playList;
    private int currentSongIndex = 0;

    // Other variables
    private Handler handler;
    private boolean isUpdatingSeekBar = false;

    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicPlaybackService.MusicBinder binder = (MusicPlaybackService.MusicBinder) service;
            musicService = binder.getService();
            isBound = true;

            // Initial setup after binding
            int songId = getIntent().getIntExtra("SONG_ID", -1);
            if (songId != -1) {
                currentSongIndex = findSongIndexById(songId);
                playSongById(songId);
            }
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

        handler = new Handler();
        database = AppDatabase.getInstance(this);

        initializeViews();
        setupListeners();
        loadPlaylist();
        bindMusicService();
    }

    private void initializeViews() {
        playPauseButton = findViewById(R.id.playPauseButton);
        nextButton = findViewById(R.id.nextButton);
        previousButton = findViewById(R.id.previousButton);
        playbackSeekBar = findViewById(R.id.playbackSeekBar);
        currentTimeTextView = findViewById(R.id.currentTimeTextView);
        totalTimeTextView = findViewById(R.id.totalTimeTextView);
        trackInfoTextView = findViewById(R.id.trackInfoTextView);
        albumCoverImageView = findViewById(R.id.albumCoverImageView);
    }

    private void setupListeners() {
        playPauseButton.setOnClickListener(v -> togglePlayPause());
        nextButton.setOnClickListener(v -> playNextSong());
        previousButton.setOnClickListener(v -> playPreviousSong());

        playbackSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && isBound && musicService != null) {
                    musicService.seekTo(progress);
                    updateCurrentTimeText(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                isUpdatingSeekBar = true;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                isUpdatingSeekBar = false;
            }
        });
    }

    private void bindMusicService() {
        Intent serviceIntent = new Intent(this, MusicPlaybackService.class);
        bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    private void loadPlaylist() {
        Executor executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            playList = database.songDao().getAllSongs();
        });
    }

    private int findSongIndexById(int songId) {
        if (playList != null) {
            for (int i = 0; i < playList.size(); i++) {
                if (playList.get(i).getSong_id() == songId) {
                    return i;
                }
            }
        }
        return 0;
    }

    private void playSongById(int songId) {
        Executor executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            Song song = database.songDao().getSongById(songId);
            runOnUiThread(() -> {
                if (song != null) {
                    updateSongInfo(song);
                    String filePath = song.getFile_path();
                    if (filePath != null && !filePath.isEmpty()) {
                        String resourceName = filePath
                                .replace("res/raw/", "")
                                .replaceAll("\\.mp3$", "")
                                .toLowerCase()
                                .replaceAll("[^a-z0-9_]", "_");

                        int resourceId = getResources().getIdentifier(
                                resourceName,
                                "raw",
                                getPackageName()
                        );

                        if (resourceId != 0) {
                            // Update the file path with the content URI for the resource
                            song.setFile_path("android.resource://" + getPackageName() + "/" + resourceId);
                            playSong(song);
                        } else {
                            Toast.makeText(this, "Không tìm thấy tài nguyên nhạc", Toast.LENGTH_SHORT).show();
                            Log.e(TAG, "Resource not found: " + resourceName);
                        }
                    }
                } else {
                    Toast.makeText(this, "Không tìm thấy bài hát", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Song not found with ID: " + songId);
                }
            });
        });
    }
    private void playSong(Song song) {
        if (isBound && musicService != null) {
            musicService.playSong(song);
            handler.postDelayed(() -> {
                setupMediaPlayerUI();
                startSeekBarUpdate();
                updatePlayPauseButton();
            }, 200);
        }
    }
    private void setupMediaPlayerUI() {
        if (isBound && musicService != null && musicService.isPrepared()) {
            int duration = musicService.getDuration();
            playbackSeekBar.setMax(duration);
            totalTimeTextView.setText(formatTime(duration));
            updatePlayPauseButton();
        }
    }

    private void updateSongInfo(Song song) {
        trackInfoTextView.setText(String.format("%s - %s", song.getTitle(), song.getArtistName()));
        // Cập nhật album art nếu có
        albumCoverImageView.setImageResource(R.drawable.default_album_art);
    }

    private void togglePlayPause() {
        if (isBound && musicService != null) {
            if (musicService.isPlaying()) {
                musicService.pauseSong();
            } else {
                musicService.resumeSong();
                startSeekBarUpdate(); // Restart seekbar updates when resuming
            }
            updatePlayPauseButton();
        }
    }

    private void updatePlayPauseButton() {
        if (isBound && musicService != null) {
            playPauseButton.setImageResource(
                    musicService.isPlaying() ? R.drawable.ic_pause : R.drawable.ic_play
            );
        }
    }

    private void playNextSong() {
        if (playList != null && !playList.isEmpty()) {
            currentSongIndex = (currentSongIndex + 1) % playList.size();
            playSongById(playList.get(currentSongIndex).getSong_id());
        }
    }

    private void playPreviousSong() {
        if (playList != null && !playList.isEmpty()) {
            currentSongIndex = (currentSongIndex - 1 + playList.size()) % playList.size();
            playSongById(playList.get(currentSongIndex).getSong_id());
        }
    }

    private void startSeekBarUpdate() {
        handler.removeCallbacksAndMessages(null); // Clear any existing callbacks
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (isBound && musicService != null) {
                    if (musicService.isPrepared()) { // Thêm kiểm tra isPrepared
                        int currentPosition = musicService.getCurrentPosition();
                        if (!isUpdatingSeekBar) {
                            playbackSeekBar.setProgress(currentPosition);
                            updateCurrentTimeText(currentPosition);
                        }
                    }
                    // Schedule the next update if music is playing
                    if (musicService.isPlaying()) {
                        handler.postDelayed(this, 1000);
                    }
                }
            }
        });
    }


    private void updateCurrentTimeText(int milliseconds) {
        currentTimeTextView.setText(formatTime(milliseconds));
    }

    private String formatTime(int milliseconds) {
        int seconds = (milliseconds / 1000) % 60;
        int minutes = (milliseconds / (1000 * 60)) % 60;
        return String.format(Locale.getDefault(), "%d:%02d", minutes, seconds);
    }

    @Override
    protected void onPause() {
        super.onPause();
        handler.removeCallbacksAndMessages(null);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isBound && musicService != null) {
            if (musicService.isPlaying()) {
                startSeekBarUpdate();
            }
            updatePlayPauseButton();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
        if (isBound) {
            unbindService(serviceConnection);
            isBound = false;
        }
    }
}