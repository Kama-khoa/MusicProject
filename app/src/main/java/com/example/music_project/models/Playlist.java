package com.example.music_project.models;

// Playlist.java
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.example.music_project.database.Converters;

import java.util.Date;

@Entity(
        foreignKeys = @ForeignKey(
                entity = User.class,
                parentColumns = "user_id",
                childColumns = "user_id",
                onDelete = ForeignKey.CASCADE
        )
)
@TypeConverters({Converters.class})  // Sử dụng Converters để xử lý kiểu Date
public class Playlist {

    @PrimaryKey(autoGenerate = true)
    public int playlist_id;
    public int user_id;
    public String title;
    public Date date_created;
    public String details;
    public String imageResource;

    public Playlist(int user_id, String title, Date date_created) {
        this.user_id = user_id;
        this.title = title;
        this.date_created = date_created;
        this.details = null;
        this.imageResource = null;
    }

    public int getPlaylist_id() {
        return playlist_id;
    }

    public void setPlaylist_id(int playlist_id) {
        this.playlist_id = playlist_id;
    }

    public int getUser_id() {
        return user_id;
    }

    public void setUser_id(int user_id) {
        this.user_id = user_id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Date getDate_created() {
        return date_created;
    }

    public void setDate_created(Date date_created) {
        this.date_created = date_created;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public String getImageResource() {
        return imageResource;
    }

    public void setImageResource(String imageResource) {
        this.imageResource = imageResource;
    }
}