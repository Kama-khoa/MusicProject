package com.example.music_project.models;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;

@Entity(
        primaryKeys = {"album_id", "song_id"},
        foreignKeys = {
                @ForeignKey(entity = Album.class, parentColumns = "album_id", childColumns = "album_id", onDelete = ForeignKey.CASCADE),
                @ForeignKey(entity = Song.class, parentColumns = "song_id", childColumns = "song_id", onDelete = ForeignKey.CASCADE)
        }
)
public class AlbumSong {
    public int album_id;
    public int song_id;
    public AlbumSong(int album_id, int song_id){
        this.album_id = album_id;
        this.song_id = song_id;
    }
    public int getAlbumId() {
        return album_id;
    }

    public void setAlbumId(int albumId) {
        this.album_id = albumId;
    }

    public int getSongId() {
        return song_id;
    }

    public void setSongId(int songId) {
        this.song_id = songId;
    }
}
