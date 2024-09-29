package com.example.music_project.models;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.ColumnInfo;
import java.util.Date;

@Entity(tableName = "Playlists")
public class Playlist {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "PlaylistID")
    private int playlistId;

    @ColumnInfo(name = "UserID")
    private int userId;

    @ColumnInfo(name = "Name")
    private String name;

    @ColumnInfo(name = "CreatedAt")
    private Date createdAt;

    public Playlist(int userId, String name) {
        this.userId = userId;
        this.name = name;
        this.createdAt = new Date();
    }

    public int getPlaylistId() {
        return playlistId;
    }

    public void setPlaylistId(int playlistId) {
        this.playlistId = playlistId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }
}
