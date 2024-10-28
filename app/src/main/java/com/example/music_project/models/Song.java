package com.example.music_project.models;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.example.music_project.database.Converters;

import java.util.Date;

@Entity(foreignKeys = {
        @ForeignKey(entity = Artist.class, parentColumns = "artist_id", childColumns = "artist_id", onDelete = ForeignKey.CASCADE),
        @ForeignKey(entity = Genre.class, parentColumns = "genre_id", childColumns = "genre_id", onDelete = ForeignKey.CASCADE)
})
@TypeConverters({Converters.class})
public class Song {

    @PrimaryKey(autoGenerate = true)
    public int song_id;
    public boolean is_sample;
    public int artist_id;
    public int genre_id;
    public String title;
    public int duration;
    public Date release_date;
    public String file_path;
    public String img_path;

    @Ignore
    private String artistName;

    public Song() {}

    public Song(String title, int artist_id, int genre_id, int duration, Date release_date, String file_path, String img_path) {
        this.title = title;
        this.artist_id = artist_id;
        this.genre_id = genre_id;
        this.duration = duration;
        this.release_date = release_date;
        this.file_path = file_path;
        this.img_path = img_path;
        this.is_sample = true;
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

    public String getImg_path() {
        return img_path;
    }

    public void setImg_path(String img_path) {
        this.img_path = img_path;
    }

    public void setIs_sample(boolean is_sample) {
        this.is_sample = is_sample;
    }

    public String getArtistName() {
        return artistName;
    }

    public void setArtistName(String artistName) {
        this.artistName = artistName;
    }
}