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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

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
    private static final String PREFS_NAME = "MusicPlayerPrefs";
    private static final String LAST_PLAYED_SONG_ID = "lastPlayedSongId";
    private static final String LAST_PLAYED_POSITION = "lastPlayedPosition";
    private SharedPreferences preferences;

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

            // Khi service được kết nối, load bài hát cuối cùng
            loadInitialSong();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            musicService = null;
            isBound = false;
            stopSeekBarUpdate();
        }
    };
    private void loadInitialSong() {
        try {
            int lastPlayedSongId = preferences.getInt(LAST_PLAYED_SONG_ID, -1);
            int lastPosition = preferences.getInt(LAST_PLAYED_POSITION, 0);

            Log.d(TAG, "Loading initial song. Last played ID: " + lastPlayedSongId +
                    ", Position: " + lastPosition);

            if (lastPlayedSongId != -1 && playList != null && !playList.isEmpty()) {
                currentSongIndex = findSongIndexById(lastPlayedSongId);
                if (currentSongIndex >= 0) {
                    Song lastSong = playList.get(currentSongIndex);
                    Log.d(TAG, "Found last song: " + lastSong.getTitle());
                    prepareAndPlaySong(lastSong, lastPosition);
                } else {
                    Log.d(TAG, "Last song not found in playlist, playing first song");
                    currentSongIndex = 0;
                    prepareAndPlaySong(playList.get(0), 0);
                }
            } else if (playList != null && !playList.isEmpty()) {
                Log.d(TAG, "No last played song, playing first song");
                currentSongIndex = 0;
                prepareAndPlaySong(playList.get(0), 0);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error loading initial song: ", e);
        }
    }
    private void prepareAndPlaySong(Song song, int position) {
        if (!isAdded() || song == null) {
            Log.e(TAG, "Fragment not added or song is null");
            return;
        }

        try {
            String filePath = song.getFile_path();
            if (filePath == null || filePath.isEmpty()) {
                Log.e(TAG, "File path is null or empty");
                return;
            }

            String resourceName = filePath
                    .replace("res/raw/", "")
                    .replaceAll("\\.mp3$", "")
                    .toLowerCase()
                    .replaceAll("[^a-z0-9_]", "_");

            Log.d(TAG, "Attempting to load resource: " + resourceName);

            int resourceId = getResources().getIdentifier(
                    resourceName,
                    "raw",
                    requireContext().getPackageName()
            );

            if (resourceId == 0) {
                Log.e(TAG, "Resource not found: " + resourceName);
                Toast.makeText(requireContext(), "Không tìm thấy file nhạc", Toast.LENGTH_SHORT).show();
                return;
            }

            // Cập nhật đường dẫn file với resourceId mới
            String fullPath = "android.resource://" + requireContext().getPackageName() + "/" + resourceId;
            song.setFile_path(fullPath);

            Log.d(TAG, "Full resource path: " + fullPath);

            // Cập nhật UI
            if (isAdded() && getActivity() != null) {
                getActivity().runOnUiThread(() -> updateSongInfo(song));
            }

            // Kiểm tra service
            if (musicService == null) {
                Log.e(TAG, "Music service is null");
                return;
            }

            // Phát nhạc
            musicService.playSong(song);

            // Đợi MediaPlayer khởi tạo xong
            handler.postDelayed(() -> {
                if (isAdded() && musicService != null && musicService.isPrepared()) {
                    try {
                        musicService.seekTo(position);
                        musicService.pauseSong();
                        updatePlayPauseButton();
                        setupMediaPlayerUI();

                        // Lưu thông tin bài hát
                        SharedPreferences.Editor editor = preferences.edit();
                        editor.putInt(LAST_PLAYED_SONG_ID, song.getSong_id());
                        editor.putInt(LAST_PLAYED_POSITION, position);
                        editor.apply();
                    } catch (Exception e) {
                        Log.e(TAG, "Error in post-preparation: ", e);
                    }
                } else {
                    Log.e(TAG, "MediaPlayer not prepared after delay");
                }
            }, 1000); // Tăng delay lên 1 giây

        } catch (Exception e) {
            Log.e(TAG, "Error preparing song: ", e);
            if (isAdded()) {
                Toast.makeText(requireContext(), "Lỗi khi chuẩn bị phát nhạc: " + e.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        handler = new Handler();
        database = AppDatabase.getInstance(requireContext());
        preferences = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        LocalBroadcastManager.getInstance(requireContext())
                .registerReceiver(songUpdateReceiver, new IntentFilter("UPDATE_SONG_INFO"));

        // Load playlist và khởi tạo service ngay từ đầu
        initializePlaylistAndService();
    }
    private void initializePlaylistAndService() {
        Executor executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            // Load playlist
            playList = database.songDao().getAllSongs();
            isPlaylistLoaded = true;

            // Sau khi load xong playlist, bind service
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    bindMusicService();
                });
            }
        });
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
                    int duration = musicService.getDuration();
                    int newPosition = (int) ((duration * progress) / 100);
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

    private void playSong(Song song) {
        if (!isBound || musicService == null || !isViewInitialized) {
            Log.e(TAG, "Service not bound or views not initialized");
            return;
        }

        try {
            // Cập nhật UI
            updateSongInfo(song);

            String filePath = song.getFile_path();

            // Nếu file_path là resource ID
            if (filePath.matches("\\d+")) {
                int resourceId = Integer.parseInt(filePath);
                musicService.playFromResourceId(resourceId, song);
            } else {
                // Xử lý file path thông thường
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
                    song.setFile_path(String.valueOf(resourceId));
                    musicService.playFromResourceId(resourceId, song);

                    // Lưu thông tin bài hát
                    saveLastPlayedSong();

                    handler.postDelayed(() -> {
                        if (isAdded() && isViewInitialized) {
                            try {
                                musicService.pauseSong();
                                setupMediaPlayerUI();
                                updatePlayPauseButton();
                            } catch (Exception e) {
                                Log.e(TAG, "Error in post-play setup: ", e);
                            }
                        }
                    }, 1000);
                } else {
                    Log.e(TAG, "Resource not found: " + resourceName);
                    Toast.makeText(requireContext(), "Không tìm thấy file nhạc", Toast.LENGTH_SHORT).show();
                    playNextSong();
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error playing song: ", e);
            if (isAdded()) {
                Toast.makeText(requireContext(), "Lỗi khi phát nhạc: " + e.getMessage(),
                        Toast.LENGTH_SHORT).show();
                playNextSong();
            }
        }
    }



    private void setupMediaPlayerUI() {
        if (!isAdded() || totalTimeTextView == null) {
            return;
        }

        if (isBound && musicService != null && musicService.isPrepared()) {
            int duration = musicService.getDuration();
            seekBar.setMax(100);
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
        boolean isPlaying = isPlaying();
        playPauseButton.setImageResource(
                isPlaying ? R.drawable.ic_pause : R.drawable.ic_play
        );

        if (!isPlaying) {
            stopSeekBarUpdate();
        }
    }
    private void playNextSong() {
        if (playList == null || playList.isEmpty()) {
            return;
        }

        int startIndex = currentSongIndex;
        boolean foundPlayableSong = false;

        do {
            currentSongIndex = (currentSongIndex + 1) % playList.size();
            if (isPlayableSong(playList.get(currentSongIndex))) {
                foundPlayableSong = true;
                break;
            }
            // Nếu đã duyệt hết playlist mà không tìm thấy bài hát nào phát được
            if (currentSongIndex == startIndex) {
                break;
            }
        } while (!foundPlayableSong);

        if (foundPlayableSong) {
            playSongById(playList.get(currentSongIndex).getSong_id());
        } else {
            if (isAdded()) {
                Toast.makeText(requireContext(),
                        "Không tìm thấy bài hát nào có thể phát",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void playPreviousSong() {
        if (playList == null || playList.isEmpty()) {
            return;
        }

        int startIndex = currentSongIndex;
        boolean foundPlayableSong = false;

        do {
            currentSongIndex = (currentSongIndex - 1 + playList.size()) % playList.size();
            if (isPlayableSong(playList.get(currentSongIndex))) {
                foundPlayableSong = true;
                break;
            }
            // Nếu đã duyệt hết playlist mà không tìm thấy bài hát nào phát được
            if (currentSongIndex == startIndex) {
                break;
            }
        } while (!foundPlayableSong);

        if (foundPlayableSong) {
            playSongById(playList.get(currentSongIndex).getSong_id());
        } else {
            if (isAdded()) {
                Toast.makeText(requireContext(),
                        "Không tìm thấy bài hát nào có thể phát",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    private boolean isPlayableSong(Song song) {
        try {
            if (song == null || song.getFile_path() == null) {
                return false;
            }

            String filePath = song.getFile_path();

            // Nếu file_path đã là resource ID
            if (filePath.matches("\\d+")) {
                return true;
            }

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

            return resourceId != 0;
        } catch (Exception e) {
            Log.e(TAG, "Error checking playable song: ", e);
            return false;
        }
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
                    if (song != null && isPlayableSong(song)) {
                        updateSongInfo(song);
                        try {
                            String filePath = song.getFile_path();
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
                                // Chỉ cần truyền resourceId cho service, không cần chuyển thành URI
                                song.setFile_path(String.valueOf(resourceId));
                                playSong(song);
                            } else {
                                Log.e(TAG, "Resource not found: " + resourceName);
                                Toast.makeText(requireContext(),
                                        "Không tìm thấy file nhạc: " + song.getTitle(),
                                        Toast.LENGTH_SHORT).show();
                                playNextSong();
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error preparing song: " + e.getMessage());
                            playNextSong();
                        }
                    } else {
                        Log.e(TAG, "Song not playable, trying next song");
                        playNextSong();
                    }
                });
            } catch (Exception e) {
                Log.e(TAG, "Error loading song: ", e);
                if (getActivity() != null) {
                    getActivity().runOnUiThread(this::playNextSong);
                }
            }
        });
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

        // Cập nhật currentSongIndex
        currentSongIndex = findSongIndexById(song.getSong_id());

        if (!isViewInitialized) {
            Log.e(TAG, "Views not initialized yet");
            return;
        }

        try {
            // Cập nhật UI với thông tin bài hát mới
            if (songTitleTextView != null) {
                songTitleTextView.setText(song.getTitle());
            }
            if (artistNameTextView != null) {
                artistNameTextView.setText(song.getArtistName());
            }
            if (albumArtImageView != null) {
                albumArtImageView.setImageResource(R.drawable.default_album_art);
            }

            // Cần đảm bảo file path được set đúng trước khi play
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
        } catch (Exception e) {
            Log.e(TAG, "Error updating song info: ", e);
        }
    }

    private void onServiceBound() {
        if (musicService != null) {
            // Lấy thông tin bài hát cuối từ SharedPreferences
            int lastPlayedSongId = preferences.getInt(LAST_PLAYED_SONG_ID, -1);
            int lastPosition = preferences.getInt(LAST_PLAYED_POSITION, 0);

            Song currentSong = musicService.getCurrentSong();
            if (currentSong != null) {
                // Service đã có bài hát, cập nhật UI
                updateSongInfo(currentSong);
                setupMediaPlayerUI();
                musicService.pauseSong();
                updatePlayPauseButton();
                stopSeekBarUpdate();
            } else if (lastPlayedSongId != -1) {
                // Service chưa có bài hát, load bài hát cuối
                loadLastPlayedSong(lastPlayedSongId, lastPosition);
            }
        }
    }

    private void loadLastPlayedSong(int songId, int position) {
        if (!isPlaylistLoaded) {
            // Đợi playlist load xong
            Executor executor = Executors.newSingleThreadExecutor();
            executor.execute(() -> {
                while (!isPlaylistLoaded) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        Log.e(TAG, "Error waiting for playlist: ", e);
                        return;
                    }
                }

                // Playlist đã load xong, tiếp tục load bài hát
                loadSongAfterPlaylistLoaded(songId, position);
            });
        } else {
            loadSongAfterPlaylistLoaded(songId, position);
        }
    }
    private void loadSongAfterPlaylistLoaded(int songId, int position) {
        try {
            Song song = database.songDao().getSongById(songId);
            if (song != null && getActivity() != null) {
                currentSongIndex = findSongIndexById(songId);

                getActivity().runOnUiThread(() -> {
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

                            // Đợi MediaPlayer khởi tạo xong và seek đến vị trí cuối
                            handler.postDelayed(() -> {
                                if (musicService != null) {
                                    musicService.seekTo(position);
                                    musicService.pauseSong();
                                    updatePlayPauseButton();
                                    setupMediaPlayerUI();
                                    stopSeekBarUpdate();
                                }
                            }, 300);
                        }
                    }
                });
            }
        } catch (Exception e) {
            Log.e(TAG, "Error loading last played song: ", e);
        }
    }
    private void saveLastPlayedSong() {
        if (musicService != null && musicService.getCurrentSong() != null) {
            SharedPreferences.Editor editor = preferences.edit();
            editor.putInt(LAST_PLAYED_SONG_ID, musicService.getCurrentSong().getSong_id());
            editor.putInt(LAST_PLAYED_POSITION, musicService.getCurrentPosition());
            editor.apply();
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
        saveLastPlayedSong(); // Lưu trạng thái khi pause
    }

    @Override
    public void onStop() {
        super.onStop();
        saveLastPlayedSong(); // Lưu trạng thái khi stop
    }
    @Override
    public void onDestroy() {
        saveLastPlayedSong(); // Lưu trạng thái trước khi destroy
        LocalBroadcastManager.getInstance(requireContext())
                .unregisterReceiver(songUpdateReceiver);

        stopSeekBarUpdate();
        if (isBound) {
            requireContext().unbindService(serviceConnection);
            isBound = false;
        }
        handler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }
    private BroadcastReceiver songUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int songId = intent.getIntExtra("SONG_ID", -1);
            if (songId != -1) {
                Executor executor = Executors.newSingleThreadExecutor();
                executor.execute(() -> {
                    Song song = database.songDao().getSongById(songId);
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            if (song != null) {
                                updateSongInfo(song);
                            }
                        });
                    }
                });
            }
        }
    };
    private boolean checkResourceExists(String resourceName) {
        int resourceId = getResources().getIdentifier(
                resourceName,
                "raw",
                requireContext().getPackageName()
        );
        return resourceId != 0;
    }
}