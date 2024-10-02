package com.example.music_project.models;

// Song.java
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;
import java.util.Date;

@Entity(foreignKeys = {
        @ForeignKey(entity = Artist.class, parentColumns = "artist_id", childColumns = "artist_id"),
        @ForeignKey(entity = Album.class, parentColumns = "album_id", childColumns = "album_id"),
        @ForeignKey(entity = Genre.class, parentColumns = "genre_id", childColumns = "genre_id")
})
public class Song {

    @PrimaryKey(autoGenerate = true)
    public int song_id;
    public boolean is_sample;
    public int artist_id;
    public int album_id;
    public int genre_id;
    public String title;
    public int duration;
    public Date release_date;
    public String file_path;

    public Song() {
    }

    public Song(String title, int artist_id, int album_id, int genre_id, int duration, Date release_date, String file_path) {
        this.title = title;
        this.artist_id = artist_id;
        this.album_id = album_id;
        this.genre_id = genre_id;
        this.duration = duration;
        this.release_date = release_date;
        this.file_path = file_path;
    }


    // Getters and Setters
    public int getSong_id() {
        return song_id;
    }

    public void setSong_id(int song_id) {
        this.song_id = song_id;
    }

    public int getArtist_id() {
        return artist_id;
    }

    public void setArtist_id(int artist_id) {
        this.artist_id = artist_id;
    }

    public int getAlbum_id() {
        return album_id;
    }

    public void setAlbum_id(int album_id) {
        this.album_id = album_id;
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

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public Date getRelease_date() {
        return release_date;
    }

    public void setRelease_date(Date release_date) {
        this.release_date = release_date;
    }

    public String getFile_path() {
        return file_path;
    }

    public void setFile_path(String file_path) {
        this.file_path = file_path;
    }

    public boolean getIs_sample() {
        return is_sample;
    }

    public void setIs_sample(boolean is_sample) {
        this.is_sample = is_sample;
    }
}