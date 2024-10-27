package com.example.music_project.models;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.ColumnInfo;
import androidx.room.PrimaryKey;

@Entity(tableName = "SongImage",  // Tên bảng
        foreignKeys = {
                @ForeignKey(
                        entity = Song.class,
                        parentColumns = "song_id",
                        childColumns = "song_id",
                        onDelete = ForeignKey.CASCADE
                )
        },
        indices = {@Index("song_id")})
public class SongImage {
    @PrimaryKey
    @ColumnInfo(name = "song_id")
    private int songId;

    @ColumnInfo(name = "image_path")
    private String imagePath;

    // Constructor
    public SongImage(int songId, String imagePath) {
        this.songId = songId;
        this.imagePath = imagePath;
    }

    // Getter và Setter cho songId
    public int getSongId() {
        return songId;
    }

    public void setSongId(int songId) {
        this.songId = songId;
    }

    // Getter và Setter cho imagePath
    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }
}