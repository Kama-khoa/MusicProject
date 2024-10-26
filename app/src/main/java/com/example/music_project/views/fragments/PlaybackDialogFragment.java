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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.music_project.R;
import com.example.music_project.database.AppDatabase;
import com.example.music_project.models.Song;
import com.example.music_project.services.MusicPlaybackService;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class PlaybackDialogFragment extends Fragment {
    private static final String TAG = "PlaybackDialogFragment";
    private static final int UPDATE_INTERVAL = 1000;

    private MusicPlaybackService musicService;
    private boolean isBound = false;
    private Handler handler;
    private boolean isUpdatingSeekBar = false;
    private boolean isViewInitialized = false;
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

    // Database and playlist
    private AppDatabase database;
    private List<Song> playList;
    private int currentSongIndex = 0;
    private boolean isPlaylistLoaded = false;

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
            stopSeekBarUpdate();
        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        handler = new Handler();
        database = AppDatabase.getInstance(requireContext());
        loadPlaylist();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_playback_dialog, container, false);
        initializeViews(view);
        setupListeners();
        bindMusicService();
        return view;
    }

    private void bindMusicService() {
        Intent serviceIntent = new Intent(requireContext(), MusicPlaybackService.class);
        requireContext().bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    private void initializeViews(View view) {
        try {
            playPauseButton = view.findViewById(R.id.playPauseButton);
            previousButton = view.findViewById(R.id.previousButton);
            nextButton = view.findViewById(R.id.nextButton);
            seekBar = view.findViewById(R.id.seekBar);
            currentTimeTextView = view.findViewById(R.id.currentTimeTextView);
            totalTimeTextView = view.findViewById(R.id.totalTimeTextView);
            songTitleTextView = view.findViewById(R.id.songTitleTextView);
            artistNameTextView = view.findViewById(R.id.artistNameTextView);
            albumArtImageView = view.findViewById(R.id.albumArtImageView);

            // Đánh dấu đã khởi tạo thành công
            isViewInitialized = true;
        } catch (Exception e) {
            Log.e(TAG, "Error initializing views: ", e);
            isViewInitialized = false;
        }
    }

    private void setupListeners() {
        playPauseButton.setOnClickListener(v -> togglePlayPause());
        nextButton.setOnClickListener(v -> playNextSong());
        previousButton.setOnClickListener(v -> playPreviousSong());

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && isBound && musicService != null) {
                    // Sửa cách tính vị trí mới
                    int duration = musicService.getDuration();
                    int newPosition = (int) ((duration * progress) / 100); // Đổi 1000 thành 100
                    musicService.seekTo(newPosition);
                    updateCurrentTimeText(newPosition);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                isUpdatingSeekBar = true;
                stopSeekBarUpdate();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                isUpdatingSeekBar = false;
                if (musicService != null && musicService.isPlaying()) {
                    startSeekBarUpdate();
                }
            }
        });
    }

    private void loadPlaylist() {
        Executor executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            playList = database.songDao().getAllSongs();
            isPlaylistLoaded = true;
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

    private void playSongById(int songId) {
        if (!isPlaylistLoaded) {
            Toast.makeText(requireContext(), "Đang tải danh sách phát...", Toast.LENGTH_SHORT).show();
            return;
        }

        Executor executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            try {
                Song song = database.songDao().getSongById(songId);
                if (getActivity() == null) return;

                getActivity().runOnUiThread(() -> {
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
                                    requireContext().getPackageName()
                            );

                            if (resourceId != 0) {
                                song.setFile_path("android.resource://" + requireContext().getPackageName() + "/" + resourceId);
                                playSong(song);
                            } else {
                                Toast.makeText(requireContext(), "Không tìm thấy tài nguyên nhạc", Toast.LENGTH_SHORT).show();
                                Log.e(TAG, "Resource not found: " + resourceName);
                            }
                        }
                    } else {
                        Toast.makeText(requireContext(), "Không tìm thấy bài hát", Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "Song not found with ID: " + songId);
                    }
                });
            } catch (Exception e) {
                Log.e(TAG, "Error loading song: ", e);
            }
        });
    }

    private void playSong(Song song) {
        if (!isBound || musicService == null || !isViewInitialized) {
            Log.e(TAG, "Service not bound or views not initialized");
            return;
        }

        try {
            musicService.playSong(song);
            handler.postDelayed(() -> {
                if (isAdded() && isViewInitialized) {
                    setupMediaPlayerUI();
                    startSeekBarUpdate();
                    updatePlayPauseButton();
                }
            }, 200);
        } catch (Exception e) {
            Log.e(TAG, "Error playing song: ", e);
        }
    }

    private void setupMediaPlayerUI() {
        if (!isAdded() || totalTimeTextView == null) {
            Log.e(TAG, "Fragment not attached or views not initialized");
            return;
        }

        if (isBound && musicService != null && musicService.isPrepared()) {
            int duration = musicService.getDuration();
            seekBar.setMax(100); // Đổi từ 1000 thành 100 để dễ tính toán phần trăm
            totalTimeTextView.setText(formatTime(duration));
            updatePlayPauseButton();
        }
    }
    private void updateSongInfo(Song song) {
        if (!isAdded()) return;
        songTitleTextView.setText(song.getTitle());
        artistNameTextView.setText(song.getArtistName());
        albumArtImageView.setImageResource(R.drawable.default_album_art);
    }

    private void togglePlayPause() {
        if (isBound && musicService != null) {
            if (musicService.isPlaying()) {
                musicService.pauseSong();
                stopSeekBarUpdate();
            } else {
                musicService.resumeSong();
                startSeekBarUpdate();
            }
            updatePlayPauseButton();
        }
    }

    private void updatePlayPauseButton() {
        if (!isAdded()) return;
        playPauseButton.setImageResource(
                isPlaying() ? R.drawable.ic_pause : R.drawable.ic_play
        );
    }

    private void playNextSong() {
        if (playList != null && !playList.isEmpty()) {
            currentSongIndex = (currentSongIndex + 1) % playList.size();
            if (currentSongIndex >= 0 && currentSongIndex < playList.size()) {
                playSongById(playList.get(currentSongIndex).getSong_id());
            }
        }
    }

    private void playPreviousSong() {
        if (playList != null && !playList.isEmpty()) {
            currentSongIndex = (currentSongIndex - 1 + playList.size()) % playList.size();
            if (currentSongIndex >= 0 && currentSongIndex < playList.size()) {
                playSongById(playList.get(currentSongIndex).getSong_id());
            }
        }
    }

    private void startSeekBarUpdate() {
        stopSeekBarUpdate(); // Xóa các callback cũ
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (isBound && musicService != null && isAdded() && !isUpdatingSeekBar) {
                    try {
                        if (musicService.isPrepared()) {
                            int currentPosition = musicService.getCurrentPosition();
                            int duration = musicService.getDuration();

                            if (duration > 0) {
                                // Tính phần trăm tiến độ (0-100)
                                int progress = (int) ((100.0 * currentPosition) / duration);
                                seekBar.setProgress(progress);
                                updateCurrentTimeText(currentPosition);
                            }
                        }

                        if (musicService.isPlaying()) {
                            handler.postDelayed(this, UPDATE_INTERVAL);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error updating seekbar: ", e);
                    }
                }
            }
        });
    }

    private void stopSeekBarUpdate() {
        handler.removeCallbacksAndMessages(null);
    }

    private void updateCurrentTimeText(int milliseconds) {
        if (currentTimeTextView != null && isAdded()) {
            currentTimeTextView.setText(formatTime(milliseconds));
        }
    }

    private String formatTime(int milliseconds) {
        int seconds = (milliseconds / 1000) % 60;
        int minutes = (milliseconds / (1000 * 60)) % 60;
        return String.format(Locale.getDefault(), "%d:%02d", minutes, seconds);
    }

    public void updateCurrentSong(Song song) {
        if (song == null) return;

        if (!isPlaylistLoaded) {
            loadPlaylist();
        }

        currentSongIndex = findSongIndexById(song.getSong_id());
        playSongById(song.getSong_id());
    }

    private void onServiceBound() {
        if (musicService != null) {
            Song currentSong = musicService.getCurrentSong();
            if (currentSong != null) {
                updateSongInfo(currentSong);
                setupMediaPlayerUI();
                if (musicService.isPlaying()) {
                    startSeekBarUpdate();
                }
            }
        }
    }

    public boolean isPlaying() {
        return isBound && musicService != null && musicService.isPlaying();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (isBound && musicService != null) {
            updatePlayPauseButton();
            if (musicService.isPlaying()) {
                startSeekBarUpdate();
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        stopSeekBarUpdate();
    }

    @Override
    public void onDestroy() {
        stopSeekBarUpdate();
        if (isBound) {
            requireContext().unbindService(serviceConnection);
            isBound = false;
        }
        handler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }
}