package com.example.music_project.models;

// Album.java
import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;
import java.util.Date;

@Entity(foreignKeys = {
        @ForeignKey(entity = Artist.class, parentColumns = "artist_id", childColumns = "artist_id", onDelete = ForeignKey.CASCADE),
        @ForeignKey(entity = Genre.class, parentColumns = "genre_id", childColumns = "genre_id", onDelete = ForeignKey.CASCADE)
})
public class Album {

    @PrimaryKey(autoGenerate = true)
    public int album_id;

    public int artist_id;
    public int genre_id;
    public String title;
    public Date release_date;
    public Album(String title, int artist_id, int genre_id, Date release_date) {
        this.title = title;
        this.artist_id = artist_id;
        this.genre_id = genre_id;
        this.release_date = release_date;
    }
    // Getters and Setters
    public int getAlbum_id() {
        return album_id;
    }

    public void setAlbum_id(int album_id) {
        this.album_id = album_id;
    }

    public int getArtist_id() {
        return artist_id;
    }

    public void setArtist_id(int artist_id) {
        this.artist_id = artist_id;
    }

    public int getGenre_id() {
        return genre_id;
    }

    public void setGenre_id(int genre_id) {
        this.genre_id = genre_id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Date getRelease_date() {
        return release_date;
    }

    public void setRelease_date(Date release_date) {
        this.release_date = release_date;
    }

    @Override
    public String toString() {
        return title;
    }
}

