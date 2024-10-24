package com.example.music_project.models;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.TypeConverters;

import com.example.music_project.database.Converters;

import java.util.Date;

@Entity(
        primaryKeys = {"user_id", "artist_id"},
        foreignKeys = {
                @ForeignKey(entity = User.class, parentColumns = "user_id", childColumns = "user_id", onDelete = ForeignKey.CASCADE),
                @ForeignKey(entity = Artist.class, parentColumns = "artist_id", childColumns = "artist_id", onDelete = ForeignKey.CASCADE)
        }
)
@TypeConverters({Converters.class})  // Sử dụng Converters để xử lý kiểu Date
public class History {

    public int user_id;
    public int artist_id;
    public Date time_listened;
    public History(int user_id, int artist_id, Date time_listened) {
        this.user_id = user_id;
        this.artist_id = artist_id;
        this.time_listened = time_listened;
    }
    public int getUser_id() {
        return user_id;
    }

    public void setUser_id(int user_id) {
        this.user_id = user_id;
    }

    public int getArtist_id() {
        return artist_id;
    }

    public void setArtist_id(int artist_id) {
        this.artist_id = artist_id;
    }

    public Date getTime_listened() {
        return time_listened;
    }

    public void setTime_listened(Date time_listened) {
        this.time_listened = time_listened;
    }
}

