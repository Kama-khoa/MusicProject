package com.example.music_project.views.fragments;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
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
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.DialogFragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.bumptech.glide.Glide;
import com.example.music_project.R;
import com.example.music_project.database.AppDatabase;
import com.example.music_project.models.Song;
import com.example.music_project.services.MusicPlaybackService;
import com.example.music_project.services.PlaybackManager;
import com.example.music_project.views.activities.FullPlaybackActivity;


public class PlaybackDialogFragment extends DialogFragment {
    private static final String TAG = "PlaybackDialogFragment";
    private static final String PREFS_NAME = "MusicAppPrefs";
    private static final String LAST_SONG_ID = "LastSongId";
    private static final String LAST_POSITION = "LastPosition";

    private ImageView albumArtImageView;
    private TextView songTitleTextView;
    private TextView artistNameTextView;
    private ImageButton playPauseButton;
    private ImageButton nextButton;
    private ImageButton previousButton;
    private SeekBar seekBar;
    private ConstraintLayout rootLayout;
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
            loadLastPlayedSong();
            updatePlaybackState();
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
                    handler.postDelayed(() -> {
                        updatePlaybackState();
                        startSeekBarUpdate();
                    }, 200);
                }
            }
        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        handler = new Handler();
        database = AppDatabase.getInstance(requireContext());
        playbackManager = new PlaybackManager(requireContext());
    }
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_playback_dialog, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initializeViews(view);
        setupListeners();
        bindMusicService();
        registerBroadcastReceiver();

    }
    private void initializeViews(View view) {
        albumArtImageView = view.findViewById(R.id.albumArtImageView);
        songTitleTextView = view.findViewById(R.id.songTitleTextView);
        artistNameTextView = view.findViewById(R.id.artistNameTextView);
        playPauseButton = view.findViewById(R.id.playPauseButton);
        nextButton = view.findViewById(R.id.nextButton);
        previousButton = view.findViewById(R.id.previousButton);
        seekBar = view.findViewById(R.id.seekBar);
        rootLayout = view.findViewById(R.id.rootLayout);


        rootLayout.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), FullPlaybackActivity.class);
            requireContext().startActivity(intent);
        });
    }

    private void setupListeners() {
        playPauseButton.setOnClickListener(v -> togglePlayPause());
        nextButton.setOnClickListener(v -> playNextSong());
        previousButton.setOnClickListener(v -> playPreviousSong());


        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && isBound && musicService != null && musicService.isPrepared()) {
                    musicService.seekTo(progress);
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
        Intent serviceIntent = new Intent(requireContext(), MusicPlaybackService.class);
        requireContext().bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    private void registerBroadcastReceiver() {
        IntentFilter filter = new IntentFilter("UPDATE_SONG_INFO");
        LocalBroadcastManager.getInstance(requireContext())
                .registerReceiver(songUpdateReceiver, filter);
    }

    private void loadSongInfo(int songId) {
        Log.d(TAG, "Loading song info for song ID: " + songId);
        new Thread(() -> {
            Song song = database.songDao().getSongById(songId);
            if (song != null) {
                Log.d(TAG, "Song loaded: " + song.getTitle());
                requireActivity().runOnUiThread(() -> updateSongInfo(song));
            } else {
                Log.d(TAG, "Song not found for ID: " + songId);
            }
        }).start();
    }
    public void updateSong(int songId) {
        if (isBound && musicService != null) {
            new Thread(() -> {
                Song song = database.songDao().getSongById(songId);
                if (song != null) {
                    requireActivity().runOnUiThread(() -> {
                        musicService.playSong(song);
                        updateSongInfo(song);

                        handler.postDelayed(() -> {
                            updatePlaybackState();
                            startSeekBarUpdate();
                        }, 200);
                    });
                }
            }).start();
        }
    }
    private void updateSongInfo(Song song) {
        songTitleTextView.setText(song.getTitle());
        String artistName = song.getArtistName();
        artistNameTextView.setText(artistName != null && !artistName.isEmpty() ? artistName : "Unknown Artist");
        String imagePath = song.getImg_path();

        if (imagePath != null) {
            if (imagePath.startsWith("res/")) {
                int resourceId = albumArtImageView.getResources().getIdentifier(
                        imagePath.replace("res/raw/", "").replace(".png", ""),
                        "raw",
                        albumArtImageView.getContext().getPackageName());

                Glide.with( albumArtImageView.getContext())
                        .load(resourceId)
                        .into(albumArtImageView);
            } else {
                Glide.with( albumArtImageView.getContext())
                        .load(imagePath)
                        .into(albumArtImageView);
            }
        } else {
            albumArtImageView.setImageResource(R.drawable.ic_image_playlist);
        }


    }

    private void updatePlaybackState() {
        if (isBound && musicService != null) {
            updatePlayPauseButton();
            if (musicService.getCurrentSong() != null) {
                updateSongInfo(musicService.getCurrentSong());
                if (musicService.isPlaying()) {
                    startSeekBarUpdate();
                }
            }
        }
    }
    private void togglePlayPause() {
        if (isBound && musicService != null) {
            if (musicService.isPlaying()) {
                musicService.pauseSong();
            } else {
                musicService.resumeSong();
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
        if (isBound && musicService != null) {
            musicService.playNextSong();
        }
    }

    private void playPreviousSong() {
        if (isBound && musicService != null) {
            musicService.playPreviousSong();
        }
    }

    private void startSeekBarUpdate() {
        handler.removeCallbacksAndMessages(null);
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (isBound && musicService != null && musicService.isPrepared()) {
                    try {
                        int currentPosition = musicService.getCurrentPosition();
                        int duration = musicService.getDuration();

                        if (duration > 0) {
                            seekBar.setMax(duration);
                            if (!isUpdatingSeekBar) {
                                seekBar.setProgress(currentPosition);
                            }
                        }

                        if (musicService.isPlaying()) {
                            handler.postDelayed(this, 100);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    @Override
    public void onPause() {
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
    public void onResume() {
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
        }
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
        if (isBound) {
            requireContext().unbindService(serviceConnection);
            isBound = false;
        }
        LocalBroadcastManager.getInstance(requireContext())
                .unregisterReceiver(songUpdateReceiver);
    }

    private void loadLastPlayedSong() {
        PlaybackManager.PlaybackState state = playbackManager.loadPlaybackState();
        if (state.isValid() && isBound && musicService != null) {
            new Thread(() -> {
                Song song = database.songDao().getSongById(state.getSongId());
                if (song != null) {
                    requireActivity().runOnUiThread(() -> {
                        musicService.setInitialLoad(true);
                        musicService.playSong(song);
                        musicService.seekTo(state.getPosition());
                        updatePlaybackState();
                    });
                }
            }).start();
        }
    }
}