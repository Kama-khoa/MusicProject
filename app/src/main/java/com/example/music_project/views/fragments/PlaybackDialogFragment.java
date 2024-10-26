package com.example.music_project.views.fragments;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.music_project.R;
import com.example.music_project.models.Song;
import com.example.music_project.services.MusicPlaybackService;

public class PlaybackDialogFragment extends Fragment {
    private static final String TAG = "PlaybackDialogFragment";
    private static final int UPDATE_INTERVAL = 500; // 500ms for smoother updates

    // Service related
    private MusicPlaybackService musicService;
    private boolean isBound = false;
    private Handler handler;
    private Song currentSong;
    private Runnable updateProgressRunnable;

    // UI Components
    private ImageButton playPauseButton;
    private ImageButton previousButton;
    private ImageButton nextButton;
    private SeekBar seekBar;
    private TextView currentTimeTextView;
    private TextView totalTimeTextView;
    private TextView songTitleTextView;
    private TextView artistNameTextView;
    private ImageView albumArtImageView;

    // State variables
    private boolean isShuffleEnabled = false;
    private boolean isRepeatEnabled = false;
    private boolean isDraggingSeekBar = false;

    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicPlaybackService.MusicBinder binder = (MusicPlaybackService.MusicBinder) service;
            musicService = binder.getService();
            isBound = true;
            onServiceBound();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            musicService = null;
            isBound = false;
            pauseProgressUpdates();
        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        handler = new Handler();
        setupUpdateRunnable();
        bindMusicService();
    }

    private void setupUpdateRunnable() {
        updateProgressRunnable = new Runnable() {
            @Override
            public void run() {
                if (isAdded() && !isRemoving()) {
                    updateProgressUI();
                    if (isPlaying()) {
                        handler.postDelayed(this, UPDATE_INTERVAL);
                    }
                }
            }
        };
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_playback_dialog, container, false);
        initializeViews(view);
        setupListeners();
        return view;
    }

    private void bindMusicService() {
        Intent serviceIntent = new Intent(requireContext(), MusicPlaybackService.class);
        requireContext().bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    private void initializeViews(View view) {
        playPauseButton = view.findViewById(R.id.playPauseButton);
        previousButton = view.findViewById(R.id.previousButton);
        nextButton = view.findViewById(R.id.nextButton);
        seekBar = view.findViewById(R.id.seekBar);
        currentTimeTextView = view.findViewById(R.id.currentTimeTextView);
        totalTimeTextView = view.findViewById(R.id.totalTimeTextView);
        songTitleTextView = view.findViewById(R.id.songTitleTextView);
        artistNameTextView = view.findViewById(R.id.artistNameTextView);
        albumArtImageView = view.findViewById(R.id.albumArtImageView);

        // Set default seekbar properties
        seekBar.setMax(1000); // Using 1000 as base for smoother seeking
    }

    private void setupListeners() {
        playPauseButton.setOnClickListener(v -> {
            if (isBound && musicService != null) {
                togglePlayPause();
            }
        });

        previousButton.setOnClickListener(v -> {
            if (isBound && musicService != null) {
                playPreviousSong();
            }
        });

        nextButton.setOnClickListener(v -> {
            if (isBound && musicService != null) {
                playNextSong();
            }
        });

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && isBound && musicService != null && isAdded() && currentTimeTextView != null) {
                    long duration = musicService.getDuration();
                    long newPosition = (duration * progress) / 1000L;
                    currentTimeTextView.setText(formatTime((int) newPosition));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                isDraggingSeekBar = true;
                pauseProgressUpdates();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (isBound && musicService != null) {
                    long duration = musicService.getDuration();
                    long newPosition = (duration * seekBar.getProgress()) / 1000L;
                    musicService.seekTo((int) newPosition);
                }
                isDraggingSeekBar = false;
                if (musicService != null && musicService.isPlaying()) {
                    resumeProgressUpdates();
                }
            }
        });
    }

    private void togglePlayPause() {
        if (!isBound || musicService == null) return;

        if (musicService.isPlaying()) {
            musicService.pauseSong();
            pauseProgressUpdates();
        } else {
            musicService.resumeSong();
            resumeProgressUpdates();
        }
        updatePlayPauseButton();
    }

    private void playPreviousSong() {
        // Implement previous song logic
        if (isBound && musicService != null) {
            // Add your previous song implementation
            updateCurrentSongUI();
            resumeProgressUpdates();
        }
    }

    private void playNextSong() {
        // Implement next song logic
        if (isBound && musicService != null) {
            // Add your next song implementation
            updateCurrentSongUI();
            resumeProgressUpdates();
        }
    }

    private void updateProgressUI() {
        if (!isBound || musicService == null || isDraggingSeekBar || !isAdded()) return;

        try {
            long currentPosition = musicService.getCurrentPosition();
            long duration = musicService.getDuration();

            if (duration > 0) {
                // Convert to scale of 1000 for smoother seeking
                int progress = (int) ((1000L * currentPosition) / duration);
                seekBar.setProgress(progress);
                currentTimeTextView.setText(formatTime((int) currentPosition));
                totalTimeTextView.setText(formatTime((int) duration));
            }

            updatePlayPauseButton();

        } catch (Exception e) {
            Log.e(TAG, "Error updating progress: " + e.getMessage());
        }
    }

    private void updatePlayPauseButton() {
        if (!isAdded()) return;

        boolean isPlaying = isBound && musicService != null && musicService.isPlaying();
        playPauseButton.setImageResource(isPlaying ? R.drawable.ic_pause : R.drawable.ic_play);
    }

    private void resumeProgressUpdates() {
        handler.removeCallbacks(updateProgressRunnable);
        handler.post(updateProgressRunnable);
    }

    private void pauseProgressUpdates() {
        handler.removeCallbacks(updateProgressRunnable);
    }

    private void onServiceBound() {
        if (musicService != null) {
            updateCurrentSongUI();
            updatePlayPauseButton();
            updateProgressUI();
            if (musicService.isPlaying()) {
                resumeProgressUpdates();
            }
        }
    }

    public void updateCurrentSong(Song song) {
        currentSong = song;
        if (isBound && musicService != null) {
            musicService.playSong(song.getFile_path());
            updateCurrentSongUI();
            resumeProgressUpdates();
        }
    }

    private void updateCurrentSongUI() {
        if (!isAdded() || currentSong == null) return;

        songTitleTextView.setText(currentSong.getTitle());
        artistNameTextView.setText(currentSong.getArtistName());
        // You can load album art here if available
        albumArtImageView.setImageResource(R.drawable.default_album_art);
    }

    private String formatTime(int milliseconds) {
        int seconds = (milliseconds / 1000) % 60;
        int minutes = (milliseconds / (1000 * 60)) % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    public boolean isPlaying() {
        return isBound && musicService != null && musicService.isPlaying();
    }

    public Song getCurrentSong() {
        return currentSong;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (isBound && musicService != null) {
            updateCurrentSongUI();
            updatePlayPauseButton();
            updateProgressUI();
            if (musicService.isPlaying()) {
                resumeProgressUpdates();
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        pauseProgressUpdates();
    }

    @Override
    public void onDestroy() {
        pauseProgressUpdates();
        if (isBound) {
            requireContext().unbindService(serviceConnection);
            isBound = false;
        }
        handler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }
}