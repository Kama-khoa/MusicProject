package com.example.music_project.models;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.ColumnInfo;
import java.util.Date;

@Entity(tableName = "PlayHistory")
public class PlayHistory {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "HistoryID")
    private int historyId;

    @ColumnInfo(name = "UserID")
    private int userId;

    @ColumnInfo(name = "SongID")
    private int songId;

    @ColumnInfo(name = "PlayedAt")
    private Date playedAt;

    public PlayHistory(int userId, int songId) {
        this.userId = userId;
        this.songId = songId;
        this.playedAt = new Date();
    }

    public int getHistoryId() {
        return historyId;
    }

    public void setHistoryId(int historyId) {
        this.historyId = historyId;
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

    public Date getPlayedAt() {
        return playedAt;
    }

    public void setPlayedAt(Date playedAt) {
        this.playedAt = playedAt;
    }
}
