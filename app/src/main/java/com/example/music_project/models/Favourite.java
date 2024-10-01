package com.example.music_project.models;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import java.util.Date;

@Entity(primaryKeys = {"user_id", "song_id"},
        foreignKeys = {
                @ForeignKey(entity = User.class, parentColumns = "user_id", childColumns = "user_id"),
                @ForeignKey(entity = Song.class, parentColumns = "song_id", childColumns = "song_id")
        })
public class Favourite {

    public int user_id;
    public int song_id;
    public Date like_date;
    public Favourite(int user_id, int song_id, Date like_date) {
        this.user_id = user_id;
        this.song_id = song_id;
        this.like_date = like_date;
    }
    // Getters and Setters
    public int getUser_id() {
        return user_id;
    }

    public void setUser_id(int user_id) {
        this.user_id = user_id;
    }

    public int getSong_id() {
        return song_id;
    }

    public void setSong_id(int song_id) {
        this.song_id = song_id;
    }

    public Date getLike_date() {
        return like_date;
    }

    public void setLike_date(Date like_date) {
        this.like_date = like_date;
    }
}
