package com.example.music_project.models;

import androidx.room.Entity;
import androidx.room.ColumnInfo;
import java.util.Date;

@Entity(tableName = "PlaylistSongs", primaryKeys = {"PlaylistID", "SongID"})
public class PlaylistSong {

    @ColumnInfo(name = "PlaylistID")
    private int playlistId;

    @ColumnInfo(name = "SongID")
    private int songId;

    @ColumnInfo(name = "AddedAt")
    private Date addedAt;

    public PlaylistSong(int playlistId, int songId) {
        this.playlistId = playlistId;
        this.songId = songId;
        this.addedAt = new Date();
    }

    public int getPlaylistId() {
        return playlistId;
    }

    public void setPlaylistId(int playlistId) {
        this.playlistId = playlistId;
    }

    public int getSongId() {
        return songId;
    }

    public void setSongId(int songId) {
        this.songId = songId;
    }

    public Date getAddedAt() {
        return addedAt;
    }

    public void setAddedAt(Date addedAt) {
        this.addedAt = addedAt;
    }
}
