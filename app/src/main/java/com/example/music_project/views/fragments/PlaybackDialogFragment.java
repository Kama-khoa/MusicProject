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

    // Service related
    private MusicPlaybackService musicService;
    private boolean isBound = false;
    private Handler handler;
    private Song currentSong;

    // UI Components
    private ImageButton playPauseButton;
    private ImageButton previousButton;
    private ImageButton nextButton;
    private ImageButton shuffleButton;
    private ImageButton repeatButton;
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
            isBound = false;
            stopPlaybackUpdates();
        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        handler = new Handler();
        bindMusicService();
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
        shuffleButton = view.findViewById(R.id.shuffleButton);
        repeatButton = view.findViewById(R.id.repeatButton);
        seekBar = view.findViewById(R.id.seekBar);
        currentTimeTextView = view.findViewById(R.id.currentTimeTextView);
        totalTimeTextView = view.findViewById(R.id.totalTimeTextView);
        songTitleTextView = view.findViewById(R.id.songTitleTextView);
        artistNameTextView = view.findViewById(R.id.artistNameTextView);
        albumArtImageView = view.findViewById(R.id.albumArtImageView);
    }

    private void setupListeners() {
        playPauseButton.setOnClickListener(v -> togglePlayPause());
        previousButton.setOnClickListener(v -> playPreviousSong());
        nextButton.setOnClickListener(v -> playNextSong());
        shuffleButton.setOnClickListener(v -> toggleShuffle());
        repeatButton.setOnClickListener(v -> toggleRepeat());

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && isBound) {
                    currentTimeTextView.setText(formatTime(progress));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                isDraggingSeekBar = true;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                isDraggingSeekBar = false;
                if (isBound) {
                    musicService.seekTo(seekBar.getProgress());
                }
            }
        });
    }

    private void onServiceBound() {
        updatePlaybackInfo();
        if (currentSong != null) {
            updateCurrentSongUI();
        }
    }

    public void updateCurrentSong(Song song) {
        currentSong = song;
        if (isBound && musicService != null) {
            musicService.playSong(song.getFile_path());
            updateCurrentSongUI();
        }
    }

    private void updateCurrentSongUI() {
        if (currentSong == null) return;

        songTitleTextView.setText(currentSong.getTitle());
        artistNameTextView.setText(currentSong.getArtistName());
        albumArtImageView.setImageResource(R.drawable.default_album_art);
    }

    private void togglePlayPause() {
        if (!isBound || musicService == null) return;

        if (musicService.isPlaying()) {
            musicService.pauseSong();
            playPauseButton.setImageResource(R.drawable.ic_play);
        } else {
            musicService.resumeSong();
            playPauseButton.setImageResource(R.drawable.ic_pause);
        }
    }

    private void playPreviousSong() {
        if (isBound && musicService != null) {
            // Implement previous song logic here
        }
    }

    private void playNextSong() {
        if (isBound && musicService != null) {
            // Implement next song logic here
        }
    }

    private void toggleShuffle() {
        isShuffleEnabled = !isShuffleEnabled;
        shuffleButton.setImageResource(isShuffleEnabled ?
                R.drawable.ic_shuffle_on : R.drawable.ic_shuffle_off);
    }

    private void toggleRepeat() {
        isRepeatEnabled = !isRepeatEnabled;
        repeatButton.setImageResource(isRepeatEnabled ?
                R.drawable.ic_repeat_on : R.drawable.ic_repeat_off);
    }

    private void updatePlaybackInfo() {
        if (!isBound || musicService == null) return;

        handler.post(new Runnable() {
            @Override
            public void run() {
                if (isBound && musicService != null && !isDraggingSeekBar) {
                    try {
                        int currentPosition = musicService.getCurrentPosition();
                        int duration = musicService.getDuration();

                        if (duration > 0) {
                            seekBar.setMax(duration);
                            seekBar.setProgress(currentPosition);
                            currentTimeTextView.setText(formatTime(currentPosition));
                            totalTimeTextView.setText(formatTime(duration));

                            playPauseButton.setImageResource(
                                    musicService.isPlaying() ? R.drawable.ic_pause : R.drawable.ic_play
                            );
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error updating playback info: " + e.getMessage());
                    }
                }
                handler.postDelayed(this, 1000);
            }
        });
    }

    private void stopPlaybackUpdates() {
        handler.removeCallbacksAndMessages(null);
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
    public void onDestroy() {
        super.onDestroy();
        stopPlaybackUpdates();
        if (isBound) {
            requireContext().unbindService(serviceConnection);
            isBound = false;
        }
    }
}