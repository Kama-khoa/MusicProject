package com.example.music_project.views.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import androidx.fragment.app.Fragment;
import com.example.music_project.R;
import com.example.music_project.controllers.PlayerController;
import com.spotify.protocol.types.Track;

public class PlayerControlsFragment extends Fragment {

    private PlayerController playerController;
    private ImageButton playPauseButton;
    private ImageButton previousButton;
    private ImageButton nextButton;
    private SeekBar seekBar;
    private TextView trackInfoTextView;
    private Handler handler;
    private Runnable updateSeekBar;

    public PlayerControlsFragment() {
        // Required empty public constructor
    }

    public static PlayerControlsFragment newInstance(PlayerController playerController) {
        PlayerControlsFragment fragment = new PlayerControlsFragment();
        fragment.playerController = playerController;
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_player_controls, container, false);

        playPauseButton = view.findViewById(R.id.play_pause_button);
        previousButton = view.findViewById(R.id.previous_button);
        nextButton = view.findViewById(R.id.next_button);
        seekBar = view.findViewById(R.id.seek_bar);
        trackInfoTextView = view.findViewById(R.id.track_info_text_view);

        handler = new Handler();

        setupListeners();
        updatePlayerState();

        return view;
    }

    private void setupListeners() {
        playPauseButton.setOnClickListener(v -> togglePlayPause());
        previousButton.setOnClickListener(v -> playerController.skipPrevious());
        nextButton.setOnClickListener(v -> playerController.skipNext());

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    playerController.seekTo(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                handler.removeCallbacks(updateSeekBar);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                handler.removeCallbacks(updateSeekBar);
                playerController.seekTo(seekBar.getProgress());
                updateSeekBar();
            }
        });
    }

    private void togglePlayPause() {
        playerController.getPlayerState(new PlayerController.PlayerStateCallback() {
            @Override
            public void onPlayerStateReceived(boolean isPlaying, Track currentTrack) {
                if (currentTrack != null) {
                    if (isPlaying) {
                        playerController.pause();
                        playPauseButton.setImageResource(android.R.drawable.ic_media_play);
                    } else {
                        playerController.resume();
                        playPauseButton.setImageResource(android.R.drawable.ic_media_pause);
                    }
                }
            }
        });
    }

    private void updatePlayerState() {
        playerController.getPlayerState(new PlayerController.PlayerStateCallback() {
            @Override
            public void onPlayerStateReceived(boolean isPlaying, Track currentTrack) {
                if (currentTrack != null) {
                    playPauseButton.setImageResource(isPlaying ?
                            android.R.drawable.ic_media_pause : android.R.drawable.ic_media_play);
                    trackInfoTextView.setText(currentTrack.name + " - " + currentTrack.artist.name);
                    // Cập nhật seekBar nếu cần
                }
            }
        });
    }

    // Phương thức này có thể được gọi từ Activity để cập nhật UI
    public void refreshPlayerState() {
        updatePlayerState();
    }
    private void updateSeekBar() {
        playerController.getPlayerState(new PlayerController.PlayerStateCallback() {
            @Override
            public void onPlayerStateReceived(boolean isPlaying, Track currentTrack) {
                if (currentTrack != null) {
                    seekBar.setProgress((int) currentTrack.duration);
                    if (isPlaying) {
                        updateSeekBar = new Runnable() {
                            @Override
                            public void run() {
                                long newPosition = seekBar.getProgress() + 1000; // Update every second
                                seekBar.setProgress((int) newPosition);
                                handler.postDelayed(this, 1000);
                            }
                        };
                        handler.postDelayed(updateSeekBar, 1000);
                    } else {
                        handler.removeCallbacks(updateSeekBar);
                    }
                }
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(updateSeekBar);
    }
}