package com.example.music_project.models;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;

@Entity(
        tableName = "album_song",
        primaryKeys = {"album_id", "song_id"},
        foreignKeys = {
                @ForeignKey(entity = Album.class, parentColumns = "album_id", childColumns = "album_id", onDelete = ForeignKey.CASCADE),
                @ForeignKey(entity = Song.class, parentColumns = "song_id", childColumns = "song_id", onDelete = ForeignKey.CASCADE)
        },
        indices = {@Index(value = {"album_id"}), @Index(value = {"song_id"})}
)
public class AlbumSong {
    public long album_id;
    public long song_id;

    public AlbumSong(long album_id, long song_id) {
        this.album_id = album_id;
        this.song_id = song_id;
    }
}
