package com.example.music_project.views.fragments;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import androidx.fragment.app.Fragment;
import com.example.music_project.R;
import com.example.music_project.services.MusicPlaybackService;
import com.example.music_project.models.Song;

public class PlayerControlsFragment extends Fragment {

    private MusicPlaybackService musicService;
    private boolean isBound = false;
    private ImageButton playPauseButton;
    private ImageButton previousButton;
    private ImageButton nextButton;
    private SeekBar seekBar;
    private TextView trackInfoTextView;
    private Handler handler = new Handler();
    private Runnable updateSeekBar;

    public PlayerControlsFragment() {
        // Required empty public constructor
    }

    public static PlayerControlsFragment newInstance() {
        return new PlayerControlsFragment();
    }

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicPlaybackService.MusicBinder binder = (MusicPlaybackService.MusicBinder) service;
            musicService = binder.getService();
            isBound = true;
            updatePlayerState();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            musicService = null;
            isBound = false;
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_player_controls, container, false);

        playPauseButton = view.findViewById(R.id.play_pause_button);
        previousButton = view.findViewById(R.id.previous_button);
        nextButton = view.findViewById(R.id.next_button);
        seekBar = view.findViewById(R.id.seek_bar);
        trackInfoTextView = view.findViewById(R.id.track_info_text_view);

        setupListeners();

        // Bind to MusicPlaybackService
        Intent intent = new Intent(getContext(), MusicPlaybackService.class);
        getContext().bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);

        return view;
    }

    private void setupListeners() {
        playPauseButton.setOnClickListener(v -> togglePlayPause());
        previousButton.setOnClickListener(v -> playPreviousSong());
        nextButton.setOnClickListener(v -> playNextSong());

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && isBound) {
                    musicService.seekTo(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                handler.removeCallbacks(updateSeekBar);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (isBound) {
                    musicService.seekTo(seekBar.getProgress());
                    updateSeekBar();
                }
            }
        });
    }

    private void togglePlayPause() {
        if (isBound) {
            if (musicService.isPlaying()) {
                musicService.pause();
                playPauseButton.setImageResource(R.drawable.ic_play);
            } else {
                musicService.resume();
                playPauseButton.setImageResource(R.drawable.ic_pause);
            }
            updateSeekBar();
        }
    }

    private void playPreviousSong() {
        if (isBound) {
            musicService.playPrevious();
            updatePlayerState();
        }
    }

    private void playNextSong() {
        if (isBound) {
            musicService.playNext();
            updatePlayerState();
        }
    }

    private void updatePlayerState() {
        if (isBound) {
            Song currentSong = musicService.getCurrentSong();
            if (currentSong != null) {
                playPauseButton.setImageResource(musicService.isPlaying() ? R.drawable.ic_pause : R.drawable.ic_play);
                trackInfoTextView.setText(currentSong.getTitle() + " - " + currentSong.getArtistName());
                seekBar.setMax(musicService.getDuration());
                updateSeekBar();
            }
        }
    }

    private void updateSeekBar() {
        if (isBound && musicService.isPlaying()) {
            seekBar.setProgress(musicService.getCurrentPosition());
            updateSeekBar = new Runnable() {
                @Override
                public void run() {
                    updateSeekBar();
                }
            };
            handler.postDelayed(updateSeekBar, 1000);
        } else {
            handler.removeCallbacks(updateSeekBar);
        }
    }

    public void refreshPlayerState() {
        updatePlayerState();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (isBound) {
            getContext().unbindService(serviceConnection);
            isBound = false;
        }
        handler.removeCallbacks(updateSeekBar);
    }
}