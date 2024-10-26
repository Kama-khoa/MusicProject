package com.example.music_project.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import com.example.music_project.R;
import com.example.music_project.models.Song;
import com.example.music_project.views.activities.PlayerActivity;

public class MediaNotificationManager {
    private static final String CHANNEL_ID = "music_playback_channel";
    private static final int NOTIFICATION_ID = 1;

    private final Context context;
    private final NotificationManagerCompat notificationManager;
    private final MediaSessionCompat mediaSession;

    public MediaNotificationManager(Context context) {
        this.context = context;
        this.notificationManager = NotificationManagerCompat.from(context);
        this.mediaSession = new MediaSessionCompat(context, "MediaSession");

        createNotificationChannel();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Music Playback",
                    NotificationManager.IMPORTANCE_LOW
            );
            channel.setDescription("Shows music playback controls");
            NotificationManager manager = context.getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }

    public Notification createNotification(Song song, boolean isPlaying) {
        if (song == null) {
            // Tạo một notification mặc định khi không có bài hát
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_music_note)
                    .setContentTitle("Music Player")
                    .setContentText("Không có bài hát đang phát")
                    .setPriority(NotificationCompat.PRIORITY_LOW);
            return builder.build();
        }
        // Intent để mở lại PlayerActivity khi click vào notification
        Intent intent = new Intent(context, PlayerActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Intent cho các nút điều khiển
        Intent prevIntent = new Intent(context, MusicPlaybackService.class)
                .setAction("PREVIOUS");
        Intent playIntent = new Intent(context, MusicPlaybackService.class)
                .setAction(isPlaying ? "PAUSE" : "PLAY");
        Intent nextIntent = new Intent(context, MusicPlaybackService.class)
                .setAction("NEXT");

        PendingIntent prevPendingIntent = PendingIntent.getService(
                context, 0, prevIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        PendingIntent playPendingIntent = PendingIntent.getService(
                context, 0, playIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        PendingIntent nextPendingIntent = PendingIntent.getService(
                context, 0, nextIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Cập nhật MediaSession metadata
        MediaMetadataCompat.Builder metadataBuilder = new MediaMetadataCompat.Builder()
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, song.getTitle())
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, String.valueOf(song.getArtist_id()));
        mediaSession.setMetadata(metadataBuilder.build());

        // Cập nhật PlaybackState
        PlaybackStateCompat.Builder stateBuilder = new PlaybackStateCompat.Builder()
                .setState(
                        isPlaying ? PlaybackStateCompat.STATE_PLAYING : PlaybackStateCompat.STATE_PAUSED,
                        PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN,
                        1.0f
                );
        mediaSession.setPlaybackState(stateBuilder.build());

        // Tạo notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_music_note) // Thêm icon này vào resources
                .setContentTitle(song.getTitle())
                .setContentText("Artist: " + song.getArtist_id())
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.sample_album_cover)) // Thêm ảnh này vào resources
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setContentIntent(pendingIntent)
                .setOnlyAlertOnce(true)
                .setShowWhen(false)
                .addAction(R.drawable.ic_previous, "Previous", prevPendingIntent)
                .addAction(isPlaying ? R.drawable.ic_pause : R.drawable.ic_play,
                        isPlaying ? "Pause" : "Play", playPendingIntent)
                .addAction(R.drawable.ic_next, "Next", nextPendingIntent)
                .setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                        .setMediaSession(mediaSession.getSessionToken())
                        .setShowActionsInCompactView(0, 1, 2));

        return builder.build();
    }

    public void updateNotification(Song song, boolean isPlaying) {
        // Thêm kiểm tra null và quyền
        if (song == null) {
            Log.w("MediaNotificationManager", "Không thể cập nhật notification: Song object là null");
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context,
                    android.Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                Log.w("MediaNotificationManager", "Không có quyền POST_NOTIFICATIONS");
                return;
            }
        }

        try {
            Notification notification = createNotification(song, isPlaying);
            notificationManager.notify(NOTIFICATION_ID, notification);
        } catch (Exception e) {
            Log.e("MediaNotificationManager", "Lỗi khi cập nhật notification", e);
        }
    }
    // Thêm method kiểm tra quyền
    public boolean areNotificationsEnabled() {
        // Kiểm tra quyền notification cho Android 13 trở lên
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return ContextCompat.checkSelfPermission(context,
                    android.Manifest.permission.POST_NOTIFICATIONS)
                    == PackageManager.PERMISSION_GRANTED;
        }
        // Với các version Android cũ hơn
        return NotificationManagerCompat.from(context).areNotificationsEnabled();
    }

    public void cancelNotification() {
        notificationManager.cancel(NOTIFICATION_ID);
        mediaSession.release();
    }
}