package com.example.music_project.models;

// PlaylistSong.java
import androidx.room.Entity;
import androidx.room.ForeignKey;

@Entity(primaryKeys = {"playlist_id", "song_id"},
        foreignKeys = {
                @ForeignKey(entity = Playlist.class, parentColumns = "playlist_id", childColumns = "playlist_id"),
                @ForeignKey(entity = Song.class, parentColumns = "song_id", childColumns = "song_id")
        })
public class PlaylistSong {

    public int playlist_id;
    public int song_id;
    public int duration;
    public  PlaylistSong(int playlist_id,int song_id){
        this.playlist_id=playlist_id;
        this.song_id=song_id;

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
