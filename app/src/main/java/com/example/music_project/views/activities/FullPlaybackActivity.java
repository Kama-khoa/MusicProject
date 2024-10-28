package com.example.music_project.views.activities;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.music_project.R;
import com.example.music_project.database.AppDatabase;
import com.example.music_project.models.Song;
import com.example.music_project.services.MusicPlaybackService;
import com.example.music_project.services.PlaybackManager;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class FullPlaybackActivity extends AppCompatActivity {
    private ImageButton backButton;
    private ImageButton playPauseButton;
    private ImageButton nextButton;
    private ImageButton previousButton;
    private ImageButton shuffleButton;
    private ImageButton timerButton;
    private SeekBar playbackSeekBar;
    private TextView trackInfoTextView;
    private TextView artistTextView;
    private TextView currentTimeTextView;
    private TextView totalTimeTextView;
    private PlaybackManager playbackManager;
    private MusicPlaybackService musicService;
    private boolean isBound = false;
    private Handler handler;
    private boolean isUpdatingSeekBar = false;
    private AppDatabase database;

    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicPlaybackService.MusicBinder binder = (MusicPlaybackService.MusicBinder) service;
            musicService = binder.getService();
            isBound = true;
            updatePlaybackState();
            updateSongInfo();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            musicService = null;
            isBound = false;
        }
    };

    private final BroadcastReceiver songUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if ("UPDATE_SONG_INFO".equals(intent.getAction())) {
                int songId = intent.getIntExtra("SONG_ID", -1);
                if (songId != -1) {
                    loadSongInfo(songId);
                    // Reset and restart seekbar updates
                    handler.removeCallbacksAndMessages(null);
                    if (isBound && musicService != null && musicService.isPlaying()) {
                        startSeekBarUpdate();
                    }
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_playback);

        // Add this line
        playbackManager = new PlaybackManager(this);

        initializeViews();
        setupListeners();
        handler = new Handler();
        database = AppDatabase.getInstance(this);
        bindMusicService();
        registerBroadcastReceiver();
    }
    @Override
    protected void onPause() {
        super.onPause();
        handler.removeCallbacksAndMessages(null);
        if (isBound && musicService != null && musicService.getCurrentSong() != null) {
            playbackManager.savePlaybackState(
                    musicService.getCurrentSong().getSong_id(),
                    musicService.getCurrentPosition(),
                    musicService.isPlaying()
            );
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        if (isBound && musicService != null) {
            PlaybackManager.PlaybackState state = playbackManager.loadPlaybackState();
            if (state.isValid()) {
                if (musicService.getCurrentSong() != null &&
                        musicService.getCurrentSong().getSong_id() == state.getSongId()) {
                    musicService.seekTo(state.getPosition());
                    if (state.wasPlaying() && !musicService.isPlaying()) {
                        musicService.resumeSong();
                    }
                }
            }
            updatePlaybackState();
            // If music is playing, ensure seekbar updates are running
            if (musicService.isPlaying()) {
                startSeekBarUpdate();
            }
        }
    }
    private void initializeViews() {
        backButton = findViewById(R.id.backButton);
        playPauseButton = findViewById(R.id.playPauseButton);
        nextButton = findViewById(R.id.nextButton);
        previousButton = findViewById(R.id.previousButton);
        shuffleButton = findViewById(R.id.shuffleButton);
        timerButton = findViewById(R.id.timerButton);
        playbackSeekBar = findViewById(R.id.playbackSeekBar);
        trackInfoTextView = findViewById(R.id.trackInfoTextView);
        artistTextView = findViewById(R.id.artistTextView);
        currentTimeTextView = findViewById(R.id.currentTimeTextView);
        totalTimeTextView = findViewById(R.id.totalTimeTextView);
    }

    private void setupListeners() {
        backButton.setOnClickListener(v -> finish());

        playPauseButton.setOnClickListener(v -> togglePlayPause());
        nextButton.setOnClickListener(v -> playNextSong());
        previousButton.setOnClickListener(v -> playPreviousSong());

        playbackSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && isBound && musicService != null) {
                    musicService.seekTo(progress);
                    updateTimeLabels(progress, seekBar.getMax());
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                isUpdatingSeekBar = true;
                handler.removeCallbacksAndMessages(null);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                isUpdatingSeekBar = false;
                if (isBound && musicService != null && musicService.isPlaying()) {
                    startSeekBarUpdate();
                }
            }
        });
    }

    private void bindMusicService() {
        Intent serviceIntent = new Intent(this, MusicPlaybackService.class);
        bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    private void registerBroadcastReceiver() {
        IntentFilter filter = new IntentFilter("UPDATE_SONG_INFO");
        LocalBroadcastManager.getInstance(this).registerReceiver(songUpdateReceiver, filter);
    }

    private void updatePlaybackState() {
        if (isBound && musicService != null) {
            updatePlayPauseButton();
            if (musicService.isPlaying()) {
                startSeekBarUpdate();
            }
        }
    }

    private void updatePlayPauseButton() {
        if (isBound && musicService != null) {
            playPauseButton.setImageResource(
                    musicService.isPlaying() ? R.drawable.ic_pause : R.drawable.ic_play
            );
        }
    }

    private void togglePlayPause() {
        if (isBound && musicService != null) {
            if (musicService.isPlaying()) {
                musicService.pauseSong();
                handler.removeCallbacksAndMessages(null); // Stop seekbar updates when paused
            } else {
                musicService.resumeSong();
                startSeekBarUpdate(); // Restart seekbar updates when resuming
            }
            updatePlayPauseButton();
        }
    }

    private void playNextSong() {
        if (isBound && musicService != null) {
            musicService.playNextSong();
            // Reset and restart seekbar updates for the new song
            handler.removeCallbacksAndMessages(null);
            handler.postDelayed(() -> {
                updateSongInfo();
                if (musicService.isPlaying()) {
                    startSeekBarUpdate();
                }
            }, 100); // Small delay to ensure service has updated
        }
    }

    private void playPreviousSong() {
        if (isBound && musicService != null) {
            musicService.playPreviousSong();
            // Reset and restart seekbar updates for the new song
            handler.removeCallbacksAndMessages(null);
            handler.postDelayed(() -> {
                updateSongInfo();
                if (musicService.isPlaying()) {
                    startSeekBarUpdate();
                }
            }, 100); // Small delay to ensure service has updated
        }
    }

    private void loadSongInfo(int songId) {
        new Thread(() -> {
            Song song = database.songDao().getSongById(songId);
            if (song != null) {
                runOnUiThread(() -> {
                    updateSongInfo(song);
                    // Ensure seekbar is properly initialized for the new song
                    if (isBound && musicService != null) {
                        playbackSeekBar.setMax(musicService.getDuration());
                        playbackSeekBar.setProgress(musicService.getCurrentPosition());
                        updateTimeLabels(musicService.getCurrentPosition(), musicService.getDuration());
                        if (musicService.isPlaying()) {
                            startSeekBarUpdate();
                        }
                    }
                });
            }
        }).start();
    }

    private void updateSongInfo() {
        if (isBound && musicService != null && musicService.getCurrentSong() != null) {
            updateSongInfo(musicService.getCurrentSong());
        }
    }

    private void updateSongInfo(Song song) {
        trackInfoTextView.setText(song.getTitle());
        artistTextView.setText(song.getArtistName() != null && !song.getArtistName().isEmpty()
                ? song.getArtistName() : "Unknown Artist");

        if (isBound && musicService != null) {
            playbackSeekBar.setMax(musicService.getDuration());
            updateTimeLabels(musicService.getCurrentPosition(), musicService.getDuration());
        }
    }

    private void startSeekBarUpdate() {
        handler.removeCallbacksAndMessages(null); // Clear any existing callbacks
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (isBound && musicService != null && !isUpdatingSeekBar) {
                    int currentPosition = musicService.getCurrentPosition();
                    int duration = musicService.getDuration();

                    if (currentPosition <= duration) {  // Add bounds checking
                        playbackSeekBar.setMax(duration);
                        playbackSeekBar.setProgress(currentPosition);
                        updateTimeLabels(currentPosition, duration);
                    }

                    // Only post delayed if still playing and bound
                    if (musicService.isPlaying() && isBound) {
                        handler.postDelayed(this, 100);
                    }
                }
            }
        });
    }

    private void updateTimeLabels(int currentPosition, int duration) {
        currentTimeTextView.setText(formatTime(currentPosition));
        totalTimeTextView.setText(formatTime(duration));
    }

    private String formatTime(int milliseconds) {
        long minutes = TimeUnit.MILLISECONDS.toMinutes(milliseconds);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(milliseconds) -
                TimeUnit.MINUTES.toSeconds(minutes);
        return String.format(Locale.getDefault(), "%d:%02d", minutes, seconds);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
        if (isBound) {
            unbindService(serviceConnection);
            isBound = false;
        }
        LocalBroadcastManager.getInstance(this)
                .unregisterReceiver(songUpdateReceiver);
    }
}