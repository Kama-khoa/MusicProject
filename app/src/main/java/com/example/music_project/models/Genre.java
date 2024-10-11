package com.example.music_project.models;

// Genre.java
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Genre {

    @PrimaryKey(autoGenerate = true)
    public int genre_id;
    public String genre_name;
    public Genre(String genre_name) {
        this.genre_name = genre_name;
    }

    // Getters and Setters
    public int getGenre_id() {
        return genre_id;
    }

    public void setGenre_id(int genre_id) {
        this.genre_id = genre_id;
    }

    public String getGenre_name() {
        return genre_name;
    }

    public void setGenre_name(String genre_name) {
        this.genre_name = genre_name;
    }

    public String toString() {
        return genre_name; // Trả về tên thể loại
    }
}

