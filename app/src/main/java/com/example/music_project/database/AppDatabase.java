package com.example.music_project.database;

import android.content.Context;
import android.util.Log;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.example.music_project.models.*;

@Database(entities = {User.class, Artist.class, Album.class, Genre.class, Song.class, Playlist.class, PlaylistSong.class, Favourite.class, History.class, AlbumSong.class}, version = 8)
@TypeConverters({Converters.class})
public abstract class AppDatabase extends RoomDatabase {
    private static final String DATABASE_NAME = "music_app_database_8";

    private static volatile AppDatabase instance;

    public abstract UserDao userDao();
    public abstract ArtistDao artistDao();
    public abstract AlbumDao albumDao();
    public abstract GenreDao genreDao();
    public abstract SongDao songDao();
    public abstract PlaylistDao playlistDao();
    public abstract PlaylistSongDao playlistSongDao();
    public abstract FavouriteDao favouriteDao();
    public abstract HistoryDao historyDao();
    public abstract AlbumSongDao albumSongDao(); // Thêm DAO mới

    public static AppDatabase getInstance(final Context context) {
        Log.d("AppDatabase", "starting");
        if (instance == null) {
            synchronized (AppDatabase.class) {
                if (instance == null) {
                    instance = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, DATABASE_NAME)
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return instance;
    }
}