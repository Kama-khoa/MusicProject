package com.example.music_project.services;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.music_project.database.AppDatabase;
import com.example.music_project.models.Song;

import java.io.File;
import java.io.FileInputStream;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class MusicPlaybackService extends Service {
    private static final String TAG = "MusicPlaybackService";
    private final IBinder binder = new MusicBinder();
    private MediaPlayer mediaPlayer;
    private boolean isPrepared = false;
    private Song currentSong = null;
    private List<Song> playlist;
    private int currentSongIndex = -1;
    private AppDatabase database;
    private boolean isShuffleEnabled = false;
    private boolean isRepeatEnabled = false;
    // Thêm biến để track lần load đầu tiên
    private boolean isInitialLoad = false;

    // Thêm setter cho isInitialLoad
    public void setInitialLoad(boolean initialLoad) {
        isInitialLoad = initialLoad;
    }
    public class MusicBinder extends Binder {
        public MusicPlaybackService getService() {
            return MusicPlaybackService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        database = AppDatabase.getInstance(this);
        loadPlaylist();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }
//mới đầu vào sẽ dùng cái này load toàn bộ bh tạo thành playlist
    private void loadPlaylist() {
        Executor executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            playlist = database.songDao().getAllSongs();
        });
    }

    public void playSong(Song song) {
        if (song == null || song.getFile_path() == null) {
            Log.e(TAG, "Invalid song or file path");
            Toast.makeText(this, "Cannot play song: Invalid song data", Toast.LENGTH_SHORT).show();
            return;
        }
        currentSong = song;
        updateCurrentSongIndex();
        isPrepared = false;

        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }

        try {
            mediaPlayer = new MediaPlayer();
            String filePath = song.getFile_path();

            // Log detailed file information
            Log.d(TAG, "Attempting to play file:");
            Log.d(TAG, "File path: " + filePath);

            // Handle different types of file paths
            if (filePath.startsWith("res/raw/")) {
                // Handle resource files
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
                    try {
                        // Update file path with content URI for the resource
                        String contentUri = "android.resource://" + getPackageName() + "/" + resourceId;
                        song.setFile_path(contentUri);
                        mediaPlayer.setDataSource(getApplicationContext(), Uri.parse(contentUri));
                    } catch (Exception e) {
                        Log.e(TAG, "Error setting data source from resource: " + e.getMessage());
                        throw e;
                    }
                } else {
                    Log.e(TAG, "Resource not found: " + resourceName);
                    Toast.makeText(this, "Không tìm thấy tài nguyên nhạc", Toast.LENGTH_SHORT).show();
                    return;
                }
            } else {
                // Handle file system and content URI paths
                File file = new File(filePath);
                Log.d(TAG, "File exists: " + file.exists());
                Log.d(TAG, "File can read: " + file.canRead());
                Log.d(TAG, "File length: " + file.length());

                if (!file.exists()) {
                    // Handle content:// URIs
                    if (filePath.startsWith("content://")) {
                        try {
                            mediaPlayer.setDataSource(getApplicationContext(), Uri.parse(filePath));
                        } catch (Exception e) {
                            Log.e(TAG, "Error setting data source from content URI: " + e.getMessage());
                            throw e;
                        }
                    }
                    // Handle file:// URIs
                    else if (filePath.startsWith("file://")) {
                        try {
                            mediaPlayer.setDataSource(getApplicationContext(), Uri.parse(filePath));
                        } catch (Exception e) {
                            Log.e(TAG, "Error setting data source from file URI: " + e.getMessage());
                            throw e;
                        }
                    } else {
                        Log.e(TAG, "File not found: " + filePath);
                        Toast.makeText(this, "File not found: " + filePath, Toast.LENGTH_LONG).show();
                        return;
                    }
                } else {
                    // File exists, use FileInputStream
                    try (FileInputStream fis = new FileInputStream(file)) {
                        mediaPlayer.setDataSource(fis.getFD());
                    } catch (Exception e) {
                        Log.e(TAG, "Error setting data source from FileInputStream: " + e.getMessage());
                        throw e;
                    }
                }
            }

            mediaPlayer.setOnPreparedListener(mp -> {
                isPrepared = true;
                if (!isInitialLoad) {
                    // Nếu không phải lần load đầu, play như bình thường
                    mp.start();
                } else {
                    // Nếu là lần load đầu, không play và reset flag
                    isInitialLoad = false;
                }
                broadcastSongChange();

                // Send broadcast to update PlaybackDialogFragment
                Intent intent = new Intent("UPDATE_SONG_INFO");
                intent.putExtra("SONG_ID", song.getSong_id());
                LocalBroadcastManager.getInstance(this).sendBroadcast(intent);

                Log.d(TAG, "Successfully prepared: " + song.getTitle());
            });

            mediaPlayer.setOnErrorListener((mp, what, extra) -> {
                String errorMessage = "MediaPlayer error: " + what + ", " + extra;
                Log.e(TAG, errorMessage);
                Toast.makeText(getApplicationContext(), errorMessage, Toast.LENGTH_LONG).show();
                isPrepared = false;
                return false;
            });

            mediaPlayer.setOnCompletionListener(mp -> {
                Log.d(TAG, "Song completed: " + song.getTitle());
                isPrepared = false;
                handleSongCompletion();
            });

            mediaPlayer.prepareAsync();

        } catch (Exception e) {
            String errorMessage = "Error playing song: " + e.getMessage();
            Log.e(TAG, errorMessage, e);
            Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
            isPrepared = false;
        }
    }
    private void handleSongCompletion() {
        if (isRepeatEnabled) {
            // Repeat current song
            playSong(currentSong);
        } else {
            // Play next song
            playNextSong();
        }
    }

    private void broadcastSongChange() {
        Intent intent = new Intent("UPDATE_SONG_INFO");
        intent.putExtra("SONG_ID", currentSong.getSong_id());
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private void updateCurrentSongIndex() {
        if (currentSong != null && playlist != null) {
            for (int i = 0; i < playlist.size(); i++) {
                if (playlist.get(i).getSong_id() == currentSong.getSong_id()) {
                    currentSongIndex = i;
                    break;
                }
            }
        }
    }

    public void playNextSong() {
        if (playlist != null && !playlist.isEmpty()) {
            if (isShuffleEnabled) {
                // Play random song except current
                int nextIndex;
                do {
                    nextIndex = (int) (Math.random() * playlist.size());
                } while (nextIndex == currentSongIndex && playlist.size() > 1);
                currentSongIndex = nextIndex;
            } else {
                // Play next song in order
                currentSongIndex = (currentSongIndex + 1) % playlist.size();
            }
            playSong(playlist.get(currentSongIndex));
        }
    }

    public void playPreviousSong() {
        if (playlist != null && !playlist.isEmpty()) {
            if (isShuffleEnabled) {
                // Play random song except current
                int prevIndex;
                do {
                    prevIndex = (int) (Math.random() * playlist.size());
                } while (prevIndex == currentSongIndex && playlist.size() > 1);
                currentSongIndex = prevIndex;
            } else {
                // Play previous song in order
                currentSongIndex = (currentSongIndex - 1 + playlist.size()) % playlist.size();
            }
            playSong(playlist.get(currentSongIndex));
        }
    }

    public boolean isPlaying() {
        return mediaPlayer != null && mediaPlayer.isPlaying();
    }

    public void pauseSong() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
        }
    }

    public void resumeSong() {
        if (mediaPlayer != null && !mediaPlayer.isPlaying() && isPrepared) {
            mediaPlayer.start();
        }
    }

    public int getCurrentPosition() {
        if (mediaPlayer != null && isPrepared) {
            return mediaPlayer.getCurrentPosition();
        }
        return 0;
    }

    public int getDuration() {
        if (mediaPlayer != null && isPrepared) {
            return mediaPlayer.getDuration();
        }
        return 0;
    }

    public void seekTo(int position) {
        if (mediaPlayer != null && isPrepared) {
            mediaPlayer.seekTo(position);
        }
    }

    public void stopSong() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
            isPrepared = false;
        }
    }

    public Song getCurrentSong() {
        return currentSong;
    }

    public boolean isPrepared() {
        return isPrepared;
    }

    public void setShuffleEnabled(boolean enabled) {
        isShuffleEnabled = enabled;
    }

    public boolean isShuffleEnabled() {
        return isShuffleEnabled;
    }

    public void setRepeatEnabled(boolean enabled) {
        isRepeatEnabled = enabled;
    }

    public boolean isRepeatEnabled() {
        return isRepeatEnabled;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
            isPrepared = false;
        }
    }

    public void playFromResourceId(int resourceId, Song song) {
        try {
            if (mediaPlayer == null) {
                mediaPlayer = new MediaPlayer();
            } else {
                mediaPlayer.reset();
            }

            mediaPlayer.setDataSource(getApplicationContext(),
                    Uri.parse("android.resource://" + getPackageName() + "/" + resourceId));

            currentSong = song;
            updateCurrentSongIndex();

            mediaPlayer.setOnPreparedListener(mp -> {
                isPrepared = true;
                mp.start();
                broadcastSongChange();
            });

            mediaPlayer.setOnCompletionListener(mp -> {
                isPrepared = false;
                handleSongCompletion();
            });

            mediaPlayer.setOnErrorListener((mp, what, extra) -> {
                Log.e(TAG, "Error playing from resource: " + what + ", " + extra);
                isPrepared = false;
                return false;
            });

            mediaPlayer.prepareAsync();

        } catch (Exception e) {
            Log.e(TAG, "Error playing from resource: ", e);
            isPrepared = false;
        }
    }
    public void restorePlaybackState(int position, boolean shouldPlay) {
        if (mediaPlayer != null && isPrepared) {
            mediaPlayer.seekTo(position);
            if (shouldPlay && !mediaPlayer.isPlaying()) {
                mediaPlayer.start();
            }
        }
    }
}