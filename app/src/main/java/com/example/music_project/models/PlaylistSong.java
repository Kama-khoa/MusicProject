package com.example.music_project.models;

import androidx.room.Entity;
import androidx.room.ForeignKey;

@Entity(primaryKeys = {"playlist_id", "song_id"},
        foreignKeys = {
                @ForeignKey(entity = Playlist.class,
                        parentColumns = "playlist_id",
                        childColumns = "playlist_id",
                        onDelete = ForeignKey.CASCADE),
                @ForeignKey(entity = Song.class,
                        parentColumns = "song_id",
                        childColumns = "song_id",
                        onDelete = ForeignKey.CASCADE)
        })
public class PlaylistSong {

    public int playlist_id;
    public int song_id;
    public int duration;

    // Constructor
    public PlaylistSong(int playlist_id, int song_id) {
        this.playlist_id = playlist_id;
        this.song_id = song_id;
        this.duration = 0;  // Set thời lượng bài hát ngay khi khởi tạo
    }

    // Getters and Setters
    public int getPlaylist_id() {
        return playlist_id;
    }

    public void setPlaylist_id(int playlist_id) {
        this.playlist_id = playlist_id;
    }

    public int getSong_id() {
        return song_id;
    }

    public void setSong_id(int song_id) {
        this.song_id = song_id;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }
}
