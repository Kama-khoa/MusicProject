package com.example.music_project.views.activities;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.music_project.views.fragments.PlayerControlsFragment;
import com.spotify.protocol.types.PlayerState;

import androidx.appcompat.app.AppCompatActivity;

import com.example.music_project.R;
import com.example.music_project.controllers.PlayerController;
import com.spotify.protocol.types.Track;

public class PlayerActivity extends AppCompatActivity {

    private PlayerController playerController;
    private ImageView albumCoverImageView;
    private TextView trackInfoTextView;
    private SeekBar playbackSeekBar;
    private ImageButton playPauseButton;
    private ImageButton previousButton;
    private ImageButton nextButton;
    private PlayerControlsFragment playerControlFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        // Khởi tạo PlayerController như trước

        playerControlFragment = PlayerControlsFragment.newInstance(playerController);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.player_control_container, playerControlFragment)
                .commit();
    }

    private void onConnected() {
        playPauseButton.setOnClickListener(v -> togglePlayPause());
        previousButton.setOnClickListener(v -> playerController.skipPrevious());
        nextButton.setOnClickListener(v -> playerController.skipNext());
        updateTrackInfo();
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

    private void updateTrackInfo() {
        playerController.getCurrentTrack(new PlayerController.TrackCallback() {
            @Override
            public void onTrackReceived(Track track) {
                String trackInfo = track.name + " - " + track.artist.name;
                trackInfoTextView.setText(trackInfo);
                // Here you would also update the album cover image
                // and the playback seek bar position
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        playerController.disconnect();
    }
}