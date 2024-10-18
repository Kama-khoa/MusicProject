package com.example.music_project.database;

import com.example.music_project.models.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Seeder {
    public static void seedDatabase(AppDatabase db) {
        // Định dạng ngày
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");

        try {
            // Dữ liệu mẫu cho bảng User
            User user1 = new User("johndoe", "john@example.com", "password123","USER");
            User user2 = new User("janedoe", "jane@example.com", "password456","USER");
            User user3 = new User("alexsmith", "alex@example.com", "password789","USER");
            User user4 = new User("emilyjohnson", "emily@example.com", "password012","USER");
            User user5 = new User("michaelbrown", "michael@example.com", "password345","USER");

            db.userDao().insert(user1);
            db.userDao().insert(user2);
            db.userDao().insert(user3);
            db.userDao().insert(user4);
            db.userDao().insert(user5);

            // Dữ liệu mẫu cho bảng Artist
            Artist artist1 = new Artist("Nguyễn Văn A", "Một nghệ sĩ nổi tiếng", format.parse("1990-01-01"));
            Artist artist2 = new Artist("Trần Thị B", "Nghệ sĩ sáng tác", format.parse("1992-02-02"));
            Artist artist3 = new Artist("Lê Văn C", "Nghệ sĩ trẻ triển vọng", format.parse("1995-03-03"));
            Artist artist4 = new Artist("Phạm Thị D", "Nghệ sĩ với nhiều giải thưởng", format.parse("1988-04-04"));
            Artist artist5 = new Artist("Hoàng Văn E", "Nghệ sĩ nổi bật trong dòng nhạc", format.parse("1985-05-05"));

            db.artistDao().insert(artist1);
            db.artistDao().insert(artist2);
            db.artistDao().insert(artist3);
            db.artistDao().insert(artist4);
            db.artistDao().insert(artist5);

            // Dữ liệu mẫu cho bảng Genre
            Genre genre1 = new Genre("Pop");
            Genre genre2 = new Genre("Rock");
            Genre genre3 = new Genre("Hip Hop");
            Genre genre4 = new Genre("Jazz");
            Genre genre5 = new Genre("Classical");

            db.genreDao().insert(genre1);
            db.genreDao().insert(genre2);
            db.genreDao().insert(genre3);
            db.genreDao().insert(genre4);
            db.genreDao().insert(genre5);

            // Dữ liệu mẫu cho bảng Album
            Album album1 = new Album("Album 1", 1, 1,  format.parse("2023-01-01"),"G:\\Code\\MusicProject1\\app\\src\\main\\res\\drawable\\sample_album_cover.png");
            Album album2 = new Album("Album 2", 2, 1,  format.parse("2023-02-01"),"G:\\Code\\MusicProject1\\app\\src\\main\\res\\drawable\\sample_album_cover.png");
            Album album3 = new Album("Album 3", 3, 1,  format.parse("2023-03-01"),"G:\\Code\\MusicProject1\\app\\src\\main\\res\\drawable\\sample_album_cover.png");
            Album album4 = new Album("Album 4", 4, 1,  format.parse("2023-04-01"),"G:\\Code\\MusicProject1\\app\\src\\main\\res\\drawable\\sample_album_cover.png");
            Album album5 = new Album("Album 5", 5, 1,  format.parse("2023-05-01"),"G:\\Code\\MusicProject1\\app\\src\\main\\res\\drawable\\sample_album_cover.png");

            db.albumDao().insert(album1);
            db.albumDao().insert(album2);
            db.albumDao().insert(album3);
            db.albumDao().insert(album4);
            db.albumDao().insert(album5);



            // Dữ liệu mẫu cho bảng Song
            Song song1 = new Song("Chờ Nghe Bước Em Về", 1, 1, 1, 240, format.parse("2023-01-01"), "res/raw/chot_nghe_buoc_em_ve.mp3");
            Song song2 = new Song("Đông Cửa Tắt Đèn", 1, 2, 1, 200, format.parse("2023-01-01"), "res/raw/dong_cua_tat_den.mp3");
            Song song3 = new Song("Hà Nội", 2, 3, 2, 300, format.parse("2023-02-01"), "res/raw/ha_noi.mp3");
            Song song4 = new Song("Những Kẻ Mộng Mơ", 3, 4, 3, 250, format.parse("2023-03-01"), "res/raw/nhung_ke_mong_mo.mp3");
            Song song5 = new Song("Suốt Đêm", 4, 5, 4, 180, format.parse("2023-04-01"), "res/raw/suot_dem.mp3");

            db.songDao().insert(song1);
            db.songDao().insert(song2);
            db.songDao().insert(song3);
            db.songDao().insert(song4);
            db.songDao().insert(song5);

            // Dữ liệu mẫu cho bảng Playlist
            Playlist playlist1 = new Playlist(1, "Playlist 1", format.parse("2023-01-01"));
            Playlist playlist2 = new Playlist(2, "Playlist 2", format.parse("2023-02-01"));
            Playlist playlist3 = new Playlist(1, "Playlist 3", format.parse("2023-03-01"));
            Playlist playlist4 = new Playlist(2, "Playlist 4", format.parse("2023-04-01"));
            Playlist playlist5 = new Playlist(1, "Playlist 5", format.parse("2023-05-01"));

            db.playlistDao().insert(playlist1);
            db.playlistDao().insert(playlist2);
            db.playlistDao().insert(playlist3);
            db.playlistDao().insert(playlist4);
            db.playlistDao().insert(playlist5);

            // Dữ liệu mẫu cho bảng Playlist_Song
            PlaylistSong ps1 = new PlaylistSong(1, 1);
            PlaylistSong ps2 = new PlaylistSong(1, 2);
            PlaylistSong ps3 = new PlaylistSong(2, 3);
            PlaylistSong ps4 = new PlaylistSong(3, 4);
            PlaylistSong ps5 = new PlaylistSong(4, 5);

            db.playlistSongDao().insert(ps1);
            db.playlistSongDao().insert(ps2);
            db.playlistSongDao().insert(ps3);
            db.playlistSongDao().insert(ps4);
            db.playlistSongDao().insert(ps5);

            // Dữ liệu mẫu cho bảng Favourites
            Favourite fav1 = new Favourite(1, 1, format.parse("2023-01-01"));
            Favourite fav2 = new Favourite(1, 2, format.parse("2023-02-01"));
            Favourite fav3 = new Favourite(2, 3, format.parse("2023-03-01"));
            Favourite fav4 = new Favourite(3, 4, format.parse("2023-04-01"));
            Favourite fav5 = new Favourite(4, 5, format.parse("2023-05-01"));

            db.favouriteDao().insert(fav1);
            db.favouriteDao().insert(fav2);
            db.favouriteDao().insert(fav3);
            db.favouriteDao().insert(fav4);
            db.favouriteDao().insert(fav5);

            // Dữ liệu mẫu cho bảng History
            History history1 = new History(1, 1, format.parse("2023-01-01 12:00:00"));
            History history2 = new History(1, 2, format.parse("2023-01-01 13:00:00"));
            History history3 = new History(2, 3, format.parse("2023-02-01 14:00:00"));
            History history4 = new History(3, 4, format.parse("2023-03-01 15:00:00"));
            History history5 = new History(4, 5, format.parse("2023-04-01 16:00:00"));

            db.historyDao().insert(history1);
            db.historyDao().insert(history2);
            db.historyDao().insert(history3);
            db.historyDao().insert(history4);
            db.historyDao().insert(history5);

        } catch (ParseException e) {
            e.printStackTrace(); // Xử lý lỗi nếu có
        }
    }
}
