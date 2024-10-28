package com.example.music_project.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.support.v4.media.session.MediaSessionCompat;
import android.util.Log;
import android.widget.Toast;
import android.app.PendingIntent;

import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.music_project.R;
import com.example.music_project.database.AppDatabase;
import com.example.music_project.models.Song;
import com.example.music_project.views.activities.MainActivity;

import java.io.File;
import java.io.FileInputStream;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class MusicPlaybackService extends Service {
    private static final String TAG = "MusicPlaybackService";
    private static final String CHANNEL_ID = "music_playback_channel";
    private static final int NOTIFICATION_ID = 1;

    private final IBinder binder = new MusicBinder();
    private MediaPlayer mediaPlayer;
    private boolean isPrepared = false;
    private Song currentSong = null;
    private List<Song> playlist;
    private int currentSongIndex = -1;
    private AppDatabase database;
    private boolean isShuffleEnabled = false;
    private boolean isRepeatEnabled = false;

    private MediaSessionCompat mediaSession;
    private NotificationManager notificationManager;
    public static final String ACTION_PLAY = "com.example.music_project.ACTION_PLAY";
    public static final String ACTION_PAUSE = "com.example.music_project.ACTION_PAUSE";
    public static final String ACTION_PREVIOUS = "com.example.music_project.ACTION_PREVIOUS";
    public static final String ACTION_NEXT = "com.example.music_project.ACTION_NEXT";
    public static final String ACTION_STOP = "com.example.music_project.ACTION_STOP";

    private boolean isInitialLoad = false;

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

        mediaSession = new MediaSessionCompat(this, "MusicPlaybackService");


        createNotificationChannel();

        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    }
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Music Playback",
                    NotificationManager.IMPORTANCE_LOW
            );
            channel.setDescription("Controls for music playback");
            channel.setShowBadge(false);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
    private PendingIntent createActionIntent(String action) {
        Intent intent = new Intent(this, MusicPlaybackService.class);
        intent.setAction(action);
        return PendingIntent.getService(
                this,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
    }

    private void updateNotification() {
        if (currentSong == null) return;

        Intent openAppIntent = new Intent(this, MainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(
                this,
                0,
                openAppIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );


        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_music_note)
                .setContentTitle(currentSong.getTitle())
                .setContentText(currentSong.getArtistName())
                .setContentIntent(contentIntent)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setOnlyAlertOnce(true)
                .setAutoCancel(false);


        builder.addAction(R.drawable.ic_previous, "Previous", createActionIntent(ACTION_PREVIOUS));

        if (isPlaying()) {
            builder.addAction(R.drawable.ic_pause, "Pause", createActionIntent(ACTION_PAUSE));
        } else {
            builder.addAction(R.drawable.ic_play, "Play", createActionIntent(ACTION_PLAY));
        }

        builder.addAction(R.drawable.ic_next, "Next", createActionIntent(ACTION_NEXT));
        builder.addAction(R.drawable.ic_close, "Stop", createActionIntent(ACTION_STOP));

        androidx.media.app.NotificationCompat.MediaStyle mediaStyle = new androidx.media.app.NotificationCompat.MediaStyle()
                .setMediaSession(mediaSession.getSessionToken())
                .setShowActionsInCompactView(0, 1, 2);

        builder.setStyle(mediaStyle);

        Notification notification = builder.build();
        startForeground(NOTIFICATION_ID, notification);
    }
    private void broadcastPlaybackState() {
        Intent intent = new Intent("PLAYBACK_STATE_CHANGED");
        intent.putExtra("IS_PLAYING", isPlaying());
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && intent.getAction() != null) {
            switch (intent.getAction()) {
                case ACTION_PLAY:
                    resumeSong();
                    break;
                case ACTION_PAUSE:
                    pauseSong();
                    break;
                case ACTION_PREVIOUS:
                    playPreviousSong();
                    updateNotification();
                    break;
                case ACTION_NEXT:
                    playNextSong();
                    updateNotification();
                    break;
                case ACTION_STOP:
                    stopForeground(true);
                    stopSelf();
                    break;
            }
        }
        return START_NOT_STICKY;
    }


    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }
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

            Log.d(TAG, "Attempting to play file:");
            Log.d(TAG, "File path: " + filePath);

            if (filePath.startsWith("res/raw/")) {
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
                File file = new File(filePath);
                Log.d(TAG, "File exists: " + file.exists());
                Log.d(TAG, "File can read: " + file.canRead());
                Log.d(TAG, "File length: " + file.length());

                if (!file.exists()) {
                    if (filePath.startsWith("content://")) {
                        try {
                            mediaPlayer.setDataSource(getApplicationContext(), Uri.parse(filePath));
                        } catch (Exception e) {
                            Log.e(TAG, "Error setting data source from content URI: " + e.getMessage());
                            throw e;
                        }
                    }
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
                    mp.start();
                    updateNotification();
                } else {
                    isInitialLoad = false;
                }
                broadcastSongChange();
                broadcastPlaybackState();

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
        updateNotification();
    }

    private void handleSongCompletion() {
        if (isRepeatEnabled) {
            playSong(currentSong);
        } else {
            playNextSong();
        }
        updateNotification();
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
                int nextIndex;
                do {
                    nextIndex = (int) (Math.random() * playlist.size());
                } while (nextIndex == currentSongIndex && playlist.size() > 1);
                currentSongIndex = nextIndex;
            } else {
                currentSongIndex = (currentSongIndex + 1) % playlist.size();
            }
            playSong(playlist.get(currentSongIndex));
            updateNotification();
        }
    }

    public void playPreviousSong() {
        if (playlist != null && !playlist.isEmpty()) {
            if (isShuffleEnabled) {
                int prevIndex;
                do {
                    prevIndex = (int) (Math.random() * playlist.size());
                } while (prevIndex == currentSongIndex && playlist.size() > 1);
                currentSongIndex = prevIndex;
            } else {
                currentSongIndex = (currentSongIndex - 1 + playlist.size()) % playlist.size();
            }
            playSong(playlist.get(currentSongIndex));
            updateNotification();
        }
    }

    public boolean isPlaying() {
        return mediaPlayer != null && mediaPlayer.isPlaying();
    }

    public void pauseSong() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
        }
        updateNotification();
        broadcastPlaybackState();
    }

    public void resumeSong() {
        if (mediaPlayer != null && !mediaPlayer.isPlaying() && isPrepared) {
            mediaPlayer.start();
        }
        updateNotification();
        broadcastPlaybackState();
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
        stopForeground(true);
    }

}