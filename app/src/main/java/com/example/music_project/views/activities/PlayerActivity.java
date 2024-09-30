package com.example.music_project.views.activities;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import com.example.music_project.R;
import com.example.music_project.controllers.PlayerController;
import com.example.music_project.models.Song;
import java.util.ArrayList;
import java.util.List;

public class PlayerActivity extends AppCompatActivity {
    private TextView tvSongTitle, tvArtist;
    private ImageButton btnPlay, btnNext, btnPrevious;
    private SeekBar seekBar;
    private PlayerController playerController;
    private List<Song> songList;
    private int currentSongIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        initializeViews();
        playerController = new PlayerController(this);

//        songList = getIntent().getParcelableArrayListExtra("song_list");
        if (songList == null) {
            songList = new ArrayList<>();
        }
        currentSongIndex = getIntent().getIntExtra("current_song_index", 0);

        if (!songList.isEmpty()) {
            updateSongInfo(songList.get(currentSongIndex));
        }

        setListeners();
    }

    private void initializeViews() {
        tvSongTitle = findViewById(R.id.tv_song_title);
        tvArtist = findViewById(R.id.tv_artist);
        btnPlay = findViewById(R.id.btn_play);
        btnNext = findViewById(R.id.btn_next);
        btnPrevious = findViewById(R.id.btn_previous);
        seekBar = findViewById(R.id.seekbar);
    }

    private void setListeners() {
        btnPlay.setOnClickListener(v -> playerController.togglePlayPause());
        btnNext.setOnClickListener(v -> playNextSong());
        btnPrevious.setOnClickListener(v -> playPreviousSong());

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

    private void playNextSong() {
        if (!songList.isEmpty()) {
            currentSongIndex = (currentSongIndex + 1) % songList.size();
            playerController.playNext(songList, currentSongIndex);
            updateSongInfo(songList.get(currentSongIndex));
        }
    }

    private void playPreviousSong() {
        if (!songList.isEmpty()) {
            currentSongIndex = (currentSongIndex - 1 + songList.size()) % songList.size();
            playerController.playPrevious(songList, currentSongIndex);
            updateSongInfo(songList.get(currentSongIndex));
        }
    }

    private void updateSongInfo(Song song) {
        tvSongTitle.setText(song.getTitle());
        tvArtist.setText(song.getArtist());
    }
}