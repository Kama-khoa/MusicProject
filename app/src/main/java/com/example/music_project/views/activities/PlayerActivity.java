package com.example.music_project.views.activities;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.MediaPlayer;
import android.net.Uri;
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
import com.example.music_project.views.fragments.PlaybackDialogFragment;

import java.io.IOException;
import java.util.Locale;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class PlayerActivity extends AppCompatActivity {
    private MusicPlaybackService musicService;
    private AppDatabase database;
    private boolean isBound = false;
    private MediaPlayer mediaPlayer;

    private ImageButton playPauseButton;
    private ImageButton nextButton;
    private ImageButton previousButton;
    private SeekBar playbackSeekBar;
    private TextView currentTimeTextView;
    private TextView totalTimeTextView;
    private TextView trackInfoTextView;
    private ImageView albumCoverImageView;

    private boolean isPlaying = false;
    private Handler handler;
    private List<Song> playList;
    private int currentSongIndex = 0;

    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicPlaybackService.MusicBinder binder = (MusicPlaybackService.MusicBinder) service;
            musicService = binder.getService();
            isBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isBound = false;
        }
    };

    public void onSongSelected(String songPath) {
        Intent intent = new Intent("STOP_PLAYBACK");
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        Intent serviceIntent = new Intent(this, MusicPlaybackService.class);
        serviceIntent.putExtra("SONG_PATH", songPath);
        startService(serviceIntent);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Intent intent = new Intent("RESUME_PLAYBACK");
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        handler = new Handler();

        initializeViews();
        setupListeners();

        database = AppDatabase.getInstance(this);
        loadPlaylist();

        Intent serviceIntent = new Intent(this, MusicPlaybackService.class);
        bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE);

        int songId = getIntent().getIntExtra("SONG_ID", -1);
        if (songId != -1 ) {
            currentSongIndex = findSongIndexById(songId);
            playSongById(songId);
        }
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
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser ) {
                if (fromUser  && mediaPlayer != null) {
                    mediaPlayer.seekTo(progress);
                    updateCurrentTimeText(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
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

    private void togglePlayPause() {
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
                playPauseButton.setImageResource(R.drawable.ic_play);
            } else {
                mediaPlayer.start();
                playPauseButton.setImageResource(R.drawable.ic_pause);
            }
            isPlaying = mediaPlayer.isPlaying();
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
                            playSong(resourceId);
                        } else {
                            String errorMsg = "Không tìm thấy tài nguyên nhạc: " + resourceName;
                            Toast.makeText(this, errorMsg, Toast.LENGTH_SHORT).show();
                            Log.e("PlayerActivity", errorMsg);
                        }
                    } else {
                        String errorMsg = "Đường dẫn bài hát không hợp lệ: " + filePath;
                        Toast.makeText(this, "Đường dẫn bài hát không hợp lệ", Toast.LENGTH_SHORT).show();
                        Log.e("PlayerActivity", errorMsg);
                    }
                } else {
                    String errorMsg = "Không tìm thấy bài hát với ID: " + songId;
                    Toast.makeText(this, "Không tìm thấy bài hát", Toast.LENGTH_SHORT).show();
                    Log.e("PlayerActivity", errorMsg);
                }
            });
        });
    }

    private void updateSongInfo(Song song) {
        trackInfoTextView.setText(song.getTitle() + " - " + song.getArtistName());
    }

    private void playSong(int rawResourceId) {
        if (isBound && musicService != null) {
            musicService.playSong(String.valueOf(rawResourceId));
        } else {
            if (mediaPlayer != null) {
                mediaPlayer.release();
            }
            mediaPlayer = new MediaPlayer();
            try {
                Uri songUri = Uri.parse("android.resource://" + getPackageName() + "/" + rawResourceId);
                mediaPlayer.setDataSource(this, songUri);
                mediaPlayer.prepare();
                mediaPlayer.start();
                setupMediaPlayerUI();

                mediaPlayer.setOnCompletionListener(mp -> playNextSong());

            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Không thể phát bài hát", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void setupMediaPlayerUI() {
        int duration = mediaPlayer.getDuration();
        playbackSeekBar.setMax(duration);
        totalTimeTextView.setText(formatTime(duration));
        playPauseButton.setImageResource(R.drawable.ic_pause);
        isPlaying = true;
        startSeekBarUpdate();
    }

    private void startSeekBarUpdate() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                    int currentPosition = mediaPlayer.getCurrentPosition();
                    playbackSeekBar.setProgress(currentPosition);
                    updateCurrentTimeText(currentPosition);
                }
                handler.postDelayed(this, 1000);
            }
        }, 0);
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
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
        if (isBound) {
            unbindService(serviceConnection);
            isBound = false;
        }
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
}