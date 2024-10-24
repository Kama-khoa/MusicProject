package com.example.music_project.models;

// Artist.java
import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import java.util.Date;

@Entity
public class Artist {

    @PrimaryKey(autoGenerate = true)
    public int artist_id;

    public String artist_name;
    public String bio;
    public Date date_of_birth;
    public Artist(String artist_name, String bio, Date date_of_birth) {
        this.artist_name = artist_name;
        this.bio = bio;
        this.date_of_birth = date_of_birth;
    }
    // Getters and Setters
    public int getArtist_id() {
        return artist_id;
    }

    public void setArtist_id(int artist_id) {
        this.artist_id = artist_id;
    }

    public String getArtist_name() {
        return artist_name;
    }

    public void setArtist_name(String artist_name) {
        this.artist_name = artist_name;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public Date getDate_of_birth() {
        return date_of_birth;
    }

    public void setDate_of_birth(Date date_of_birth) {
        this.date_of_birth = date_of_birth;
    }

    @Override
    public String toString() {
        return artist_name;
    }
}

