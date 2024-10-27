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

            Artist artist1 = new Artist("Double2T", "N/A", format.parse("1990-01-01"));
            Artist artist2 = new Artist("T.R.I", "N/A", format.parse("1992-02-02"));
            Artist artist3 = new Artist("Song Luân", "N/A", format.parse("1995-03-03"));
            Artist artist4 = new Artist("RayO", "N/A", format.parse("1988-04-04"));
            Artist artist5 = new Artist("Orange", "N/A", format.parse("1985-05-05"));
            Artist artist6 = new Artist("RPT MCK", "N/A", format.parse("1994-06-06"));
            Artist artist7 = new Artist("RHYDER", "N/A", format.parse("1993-07-07"));
            Artist artist8 = new Artist("Tự Long", "N/A", format.parse("1991-08-08"));
            Artist artist9 = new Artist("Vũ Thịnh", "N/A", format.parse("1992-09-09"));
            Artist artist10 = new Artist("Cheng", "N/A", format.parse("1993-10-10"));
            Artist artist11 = new Artist("MONO", "N/A", format.parse("1992-11-11"));
            Artist artist12 = new Artist("Obito", "N/A", format.parse("1993-12-12"));
            Artist artist13 = new Artist("Sơn Tùng M-TP", "N/A", format.parse("1995-01-13"));
            Artist artist14 = new Artist("Quang Đăng Trần", "N/A", format.parse("1990-02-14"));
            Artist artist15 = new Artist("Kha", "N/A", format.parse("1987-03-15"));
            Artist artist16 = new Artist("HIEUTHUHAI", "N/A", format.parse("1993-04-16"));
            Artist artist17 = new Artist("GREY-D", "N/A", format.parse("1990-05-17"));
            Artist artist18 = new Artist("Bích Phương", "N/A", format.parse("1991-06-18"));
            Artist artist19 = new Artist("V.A", "N/A", format.parse("1989-07-19"));
            Artist artist20 = new Artist("PhúcXP", "N/A", format.parse("1988-08-20"));
            Artist artist21 = new Artist("SOOBIN", "N/A", format.parse("1994-09-21"));
            Artist artist22 = new Artist("Phan Mạnh Quỳnh", "N/A", format.parse("1992-10-22"));
            Artist artist23 = new Artist("Rio", "N/A", format.parse("1991-11-23"));
            Artist artist24 = new Artist("Chu Thúy Quỳnh", "N/A", format.parse("1989-12-24"));
            Artist artist25 = new Artist("Trung I.U", "N/A", format.parse("1990-01-25"));
            Artist artist26 = new Artist("Wren Evans", "N/A", format.parse("1992-02-26"));
            db.artistDao().insert(artist1);
            db.artistDao().insert(artist2);
            db.artistDao().insert(artist3);
            db.artistDao().insert(artist4);
            db.artistDao().insert(artist5);
            db.artistDao().insert(artist6);
            db.artistDao().insert(artist7);
            db.artistDao().insert(artist8);
            db.artistDao().insert(artist9);
            db.artistDao().insert(artist10);
            db.artistDao().insert(artist11);
            db.artistDao().insert(artist12);
            db.artistDao().insert(artist13);
            db.artistDao().insert(artist14);
            db.artistDao().insert(artist15);
            db.artistDao().insert(artist16);
            db.artistDao().insert(artist17);
            db.artistDao().insert(artist18);
            db.artistDao().insert(artist19);
            db.artistDao().insert(artist20);
            db.artistDao().insert(artist21);
            db.artistDao().insert(artist22);
            db.artistDao().insert(artist23);
            db.artistDao().insert(artist24);
            db.artistDao().insert(artist25);
            db.artistDao().insert(artist26);


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
            Song song1 = new Song("À Lôi", 1, 1, 1, 210, format.parse("2023-01-01"), "res/raw/a_loi.mp3");
            Song song2 = new Song("Ánh Sao Và Bầu Trời", 2, 2, 1, 200, format.parse("2023-01-02"), "res/raw/anh_sao_va_bau_troi.mp3");
            Song song3 = new Song("Anh Thích Em Như Vậy", 3, 3, 1, 230, format.parse("2023-01-03"), "res/raw/anh_thich_em_nhu_vay.mp3");
            Song song4 = new Song("Cẩm Tú Cầu", 4, 4, 1, 210, format.parse("2023-01-04"), "res/raw/cam_tu_cau.mp3");
            Song song5 = new Song("Chân Ái", 5, 5, 1, 220, format.parse("2023-01-05"), "res/raw/chan_ai.mp3");
            Song song6 = new Song("Chìm Sâu", 6, 1, 1, 215, format.parse("2023-01-06"), "res/raw/chim_sau.mp3");
            Song song7 = new Song("Chịu Cách Mình Nói Thua", 7, 1, 1, 210, format.parse("2023-01-07"), "res/raw/chiu_cach_minh_noi_thua.mp3");
            Song song8 = new Song("Chợt Nghe Bước Em Về", 8, 1, 1, 240, format.parse("2023-01-08"), "res/raw/chot_nghe_buoc_em_ve.mp3");
            Song song9 = new Song("Đợi Đến Tháng 13", 9, 3, 1, 245, format.parse("2023-01-09"), "res/raw/doi_den_thang_13.mp3");
            Song song10 = new Song("Đóng Cửa, Tắt Đèn", 10, 3, 1, 210, format.parse("2023-01-10"), "res/raw/dong_cua_tat_den.mp3");
            Song song11 = new Song("Em Xinh", 11, 4, 1, 220, format.parse("2023-01-11"), "res/raw/em_xinh.mp3");
            Song song12 = new Song("Hà Nội", 12, 5, 1, 300, format.parse("2023-01-12"), "res/raw/ha_noi.mp3");
            Song song13 = new Song("Hãy Trao Cho Anh", 13, 3, 1, 210, format.parse("2023-01-13"), "res/raw/hay_trao_cho_anh.mp3");
            Song song14 = new Song("Hoa Nở Bên Đường", 14, 4, 1, 215, format.parse("2023-01-14"), "res/raw/hoa_no_ben_duong.mp3");
            Song song15 = new Song("Hư Không", 15, 5, 1, 260, format.parse("2023-01-15"), "res/raw/hu_khong.mp3");
            Song song16 = new Song("Không Thể Say", 16, 1, 1, 230, format.parse("2023-01-16"), "res/raw/khong_the_say.mp3");
            Song song17 = new Song("Lời Tạm Biệt Chưa Nói", 17, 1, 1, 240, format.parse("2023-01-17"), "res/raw/loi_tam_biet_chua_noi.mp3");
            Song song18 = new Song("Nâng Chén Tiêu Sầu", 18, 1, 1, 250, format.parse("2023-01-18"), "res/raw/nang_chen_tieu_sau.mp3");
            Song song19 = new Song("Nắng Có Mang Em Về", 19, 1, 1, 255, format.parse("2023-01-19"), "res/raw/nang_co_mang_em_ve.mp3");
            Song song20 = new Song("Như Anh Đã Thấy Em", 20, 2, 1, 245, format.parse("2023-01-20"), "res/raw/nhu_anh_da_thay_em.mp3");
            Song song21 = new Song("Những Kẻ Mộng Mơ", 21, 2, 1, 300, format.parse("2023-01-21"), "res/raw/nhung_ke_mong_mo.mp3");
            Song song22 = new Song("Sau Lời Từ Khước", 22, 2, 1, 260, format.parse("2023-01-22"), "res/raw/sau_loi_tu_khuoc.mp3");
            Song song23 = new Song("Suốt Đêm", 23, 3, 1, 225, format.parse("2023-01-23"), "res/raw/suot_dem.mp3");
            Song song24 = new Song("Thương Ly Biệt", 24, 4, 1, 230, format.parse("2023-01-24"), "res/raw/thuong_ly_biet.mp3");
            Song song25 = new Song("Thuyền Không Bến Đợi", 25, 5, 1, 240, format.parse("2023-01-25"), "res/raw/thuyen_khong_ben_doi.mp3");
            Song song26 = new Song("Từng Quen", 26, 2, 1, 240, format.parse("2023-01-26"), "res/raw/tung_quen.mp3");
            Song song27 = new Song("Waiting For You", 27, 2, 1, 240, format.parse("2023-01-27"), "res/raw/waiting_for_you.mp3");
            db.songDao().insert(song1);
            db.songDao().insert(song2);
            db.songDao().insert(song3);
            db.songDao().insert(song4);
            db.songDao().insert(song5);
            db.songDao().insert(song6);
            db.songDao().insert(song7);
            db.songDao().insert(song8);
            db.songDao().insert(song9);
            db.songDao().insert(song10);
            db.songDao().insert(song11);
            db.songDao().insert(song12);
            db.songDao().insert(song13);
            db.songDao().insert(song14);
            db.songDao().insert(song15);
            db.songDao().insert(song16);
            db.songDao().insert(song17);
            db.songDao().insert(song18);
            db.songDao().insert(song19);
            db.songDao().insert(song20);
            db.songDao().insert(song21);
            db.songDao().insert(song22);
            db.songDao().insert(song23);
            db.songDao().insert(song24);
            db.songDao().insert(song25);
            db.songDao().insert(song26);
            db.songDao().insert(song27);

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


            // Dữ liệu mẫu cho bảng PlayHistory
            PlayHistory playHistory1 = new PlayHistory(1, 1);  // UserID 1, SongID 1
            PlayHistory playHistory2 = new PlayHistory(1, 12);  // UserID 1, SongID 2
            PlayHistory playHistory6 = new PlayHistory(1, 20);
            PlayHistory playHistory3 = new PlayHistory(2, 3);  // UserID 2, SongID 3
            PlayHistory playHistory4 = new PlayHistory(3, 4);  // UserID 3, SongID 4
            PlayHistory playHistory5 = new PlayHistory(4, 5);  // UserID 4, SongID 5

            db.playHistoryDao().insert(playHistory1);
            db.playHistoryDao().insert(playHistory6);
            db.playHistoryDao().insert(playHistory2);
            db.playHistoryDao().insert(playHistory3);
            db.playHistoryDao().insert(playHistory4);
            db.playHistoryDao().insert(playHistory5);

            // Dữ liệu mẫu cho bảng AlbumSong
            AlbumSong albumSong1 = new AlbumSong(1, 1);  // AlbumID 1, SongID 1
            AlbumSong albumSong2 = new AlbumSong(1, 2);  // AlbumID 1, SongID 2
            AlbumSong albumSong3 = new AlbumSong(2, 3);  // AlbumID 2, SongID 3
            AlbumSong albumSong4 = new AlbumSong(3, 4);  // AlbumID 3, SongID 4
            AlbumSong albumSong5 = new AlbumSong(4, 5);  // AlbumID 4, SongID 5

            db.albumSongDao().insert(albumSong1);
            db.albumSongDao().insert(albumSong2);
            db.albumSongDao().insert(albumSong3);
            db.albumSongDao().insert(albumSong4);
            db.albumSongDao().insert(albumSong5);

        } catch (ParseException e) {
            e.printStackTrace(); // Xử lý lỗi nếu có
        }
    }
}
