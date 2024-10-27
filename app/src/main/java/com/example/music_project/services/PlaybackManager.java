package com.example.music_project.services;

import android.content.Context;
import android.content.SharedPreferences;

public class PlaybackManager {
    private static final String PREFS_NAME = "MusicAppPrefs";
    private static final String LAST_SONG_ID = "LastSongId";
    private static final String LAST_POSITION = "LastPosition";
    private static final String LAST_PLAYING_STATE = "LastPlayingState";

    private final Context context;
    private final SharedPreferences prefs;

    public PlaybackManager(Context context) {
        this.context = context;
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public void savePlaybackState(int songId, int position, boolean isPlaying) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(LAST_SONG_ID, songId);
        editor.putInt(LAST_POSITION, position);
        editor.putBoolean(LAST_PLAYING_STATE, isPlaying);
        editor.apply();
    }

    public PlaybackState loadPlaybackState() {
        int songId = prefs.getInt(LAST_SONG_ID, -1);
        int position = prefs.getInt(LAST_POSITION, 0);
        boolean wasPlaying = prefs.getBoolean(LAST_PLAYING_STATE, false);

        return new PlaybackState(songId, position, wasPlaying);
    }

    public static class PlaybackState {
        private final int songId;
        private final int position;
        private final boolean wasPlaying;

        public PlaybackState(int songId, int position, boolean wasPlaying) {
            this.songId = songId;
            this.position = position;
            this.wasPlaying = wasPlaying;
        }

        public int getSongId() { return songId; }
        public int getPosition() { return position; }
        public boolean wasPlaying() { return wasPlaying; }
        public boolean isValid() { return songId != -1; }
    }
}
