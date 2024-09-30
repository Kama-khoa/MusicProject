package com.example.music_project.models;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.ColumnInfo;
import java.util.Date;

@Entity(tableName = "Songs")
public class Song {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "SongID")
    private int songId;

    @ColumnInfo(name = "Title")
    private String title;

    @ColumnInfo(name = "Artist")
    private String artist;

    @ColumnInfo(name = "Album")
    private String album;

    @ColumnInfo(name = "Duration")
    private int duration;

    @ColumnInfo(name = "FilePath")
    private String filePath;

    @ColumnInfo(name = "CreatedAt")
    private Date createdAt;

    // Constructor
    public Song(String title, String artist, String album, int duration, String filePath) {
        this.title = title;
        this.artist = artist;
        this.album = album;
        this.duration = duration;
        this.filePath = filePath;
        this.createdAt = new Date();
    }

    // Getter and setter for songId
    public int getSongId() {
        return songId;
    }

    public void setSongId(int songId) {
        this.songId = songId;
    }

    // Getter and setter for title
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    // Getter and setter for artist
    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    // Getter and setter for album
    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    // Getter and setter for duration
    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    // Getter and setter for filePath
    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    // Getter and setter for createdAt
    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public String getSongUrl() {
        return filePath;
    }
}
