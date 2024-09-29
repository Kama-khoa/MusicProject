package com.example.music_project.models;

import androidx.room.Entity;
import androidx.room.ColumnInfo;
import java.util.Date;

@Entity(tableName = "Favorites", primaryKeys = {"UserID", "SongID"})
public class Favorite {

    @ColumnInfo(name = "UserID")
    private int userId;

    @ColumnInfo(name = "SongID")
    private int songId;

    @ColumnInfo(name = "AddedAt")
    private Date addedAt;

    public Favorite(int userId, int songId) {
        this.userId = userId;
        this.songId = songId;
        this.addedAt = new Date();
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
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
