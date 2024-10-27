package com.example.music_project.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.music_project.models.Album;
import com.example.music_project.models.AlbumSong;
import com.example.music_project.models.PlaylistSong;
import com.example.music_project.models.Song;
import com.example.music_project.models.SongImage;

import java.util.List;

@Dao
public interface SongImageDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(SongImage Songimg);
}
