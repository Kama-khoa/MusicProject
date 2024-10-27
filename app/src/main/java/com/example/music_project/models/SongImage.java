package com.example.music_project.models;

public class SongImage {
    private int songId; // ID của bài hát
    private String imagePath; // Đường dẫn ảnh

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
