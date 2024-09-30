package com.example.music_project.controllers;

import android.content.Context;
import android.util.Log;

import com.spotify.android.appremote.api.ConnectionParams;
import com.spotify.android.appremote.api.Connector;
import com.spotify.android.appremote.api.SpotifyAppRemote;
import com.spotify.protocol.types.Track;

public class PlayerController {
    private static final String CLIENT_ID = "your_client_id_here";
    private static final String REDIRECT_URI = "your_redirect_uri_here";
    private SpotifyAppRemote mSpotifyAppRemote;

    public void connect(Context context, final Runnable onConnected) {
        ConnectionParams connectionParams =
                new ConnectionParams.Builder(CLIENT_ID)
                        .setRedirectUri(REDIRECT_URI)
                        .showAuthView(true)
                        .build();

        SpotifyAppRemote.connect(context, connectionParams,
                new Connector.ConnectionListener() {
                    @Override
                    public void onConnected(SpotifyAppRemote spotifyAppRemote) {
                        mSpotifyAppRemote = spotifyAppRemote;
                        onConnected.run();
                    }

                    @Override
                    public void onFailure(Throwable throwable) {
                        // Handle connection failure
                    }
                });
    }

    public void play(String spotifyUri) {
        if (mSpotifyAppRemote != null) {
            mSpotifyAppRemote.getPlayerApi().play(spotifyUri);
        }
    }

    public void pause() {
        if (mSpotifyAppRemote != null) {
            mSpotifyAppRemote.getPlayerApi().pause();
        }
    }

    public void resume() {
        if (mSpotifyAppRemote != null) {
            mSpotifyAppRemote.getPlayerApi().resume();
        }
    }

    public void skipNext() {
        if (mSpotifyAppRemote != null) {
            mSpotifyAppRemote.getPlayerApi().skipNext();
        }
    }

    public void skipPrevious() {
        if (mSpotifyAppRemote != null) {
            mSpotifyAppRemote.getPlayerApi().skipPrevious();
        }
    }

    public void getCurrentTrack(final TrackCallback callback) {
        if (mSpotifyAppRemote != null) {
            mSpotifyAppRemote.getPlayerApi()
                    .subscribeToPlayerState()
                    .setEventCallback(playerState -> {
                        final Track track = playerState.track;
                        if (track != null) {
                            callback.onTrackReceived(track);
                        }
                    });
        }
    }

    public void disconnect() {
        SpotifyAppRemote.disconnect(mSpotifyAppRemote);
    }

    public interface TrackCallback {
        void onTrackReceived(Track track);
    }
    public interface PlayerStateCallback {
        void onPlayerStateReceived(boolean isPlaying, Track currentTrack);
    }

    public void getPlayerState(PlayerStateCallback callback) {
        mSpotifyAppRemote.getPlayerApi().getPlayerState()
                .setResultCallback(playerState -> {
                    boolean isPlaying = !playerState.isPaused;
                    Track currentTrack = playerState.track;
                    callback.onPlayerStateReceived(isPlaying, currentTrack);
                })
                .setErrorCallback(throwable -> {
                    // Xử lý lỗi ở đây
                    Log.e("PlayerController", "Error getting player state", throwable);
                    callback.onPlayerStateReceived(false, null);
                });
    }

    public void seekTo(long positionMs) {
        if (mSpotifyAppRemote != null && mSpotifyAppRemote.isConnected()) {
            mSpotifyAppRemote.getPlayerApi().seekTo(positionMs);
        } else {
            Log.w("PlayerController", "SpotifyAppRemote not connected");
        }
    }

    // Thêm phương thức để lấy thời lượng của bài hát hiện tại
    public void getTrackDuration(TrackDurationCallback callback) {
        if (mSpotifyAppRemote != null && mSpotifyAppRemote.isConnected()) {
            mSpotifyAppRemote.getPlayerApi().getPlayerState()
                    .setResultCallback(playerState -> {
                        if (playerState.track != null) {
                            callback.onTrackDurationReceived(playerState.track.duration);
                        } else {
                            callback.onTrackDurationReceived(0);
                        }
                    })
                    .setErrorCallback(throwable -> {
                        Log.e("PlayerController", "Error getting track duration", throwable);
                        callback.onTrackDurationReceived(0);
                    });
        } else {
            Log.w("PlayerController", "SpotifyAppRemote not connected");
            callback.onTrackDurationReceived(0);
        }
    }

    public interface TrackDurationCallback {
        void onTrackDurationReceived(long durationMs);
    }
}