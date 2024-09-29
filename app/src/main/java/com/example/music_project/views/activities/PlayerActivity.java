package com.example.music_project.views.activities;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import com.example.music_project.R;
import com.example.music_project.controllers.PlayerController;

public class PlayerActivity extends AppCompatActivity {
    private TextView tvSongTitle, tvArtist;
    private ImageButton btnPlay, btnNext, btnPrevious;
    private SeekBar seekBar;
    private PlayerController playerController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        playerController = new PlayerController(this);

        tvSongTitle = findViewById(R.id.tv_song_title);
        tvArtist = findViewById(R.id.tv_artist);
        btnPlay = findViewById(R.id.btn_play);
        btnNext = findViewById(R.id.btn_next);
        btnPrevious = findViewById(R.id.btn_previous);
        seekBar = findViewById(R.id.seekbar);

        btnPlay.setOnClickListener(v -> playerController.togglePlayPause());
        btnNext.setOnClickListener(v -> playerController.playNext());
        btnPrevious.setOnClickListener(v -> playerController.playPrevious());

        // Setup seekbar change listener
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    playerController.seekTo(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    // Add methods to update UI based on playback state
}
