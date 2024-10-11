package com.example.music_project.views.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.music_project.R;
import com.example.music_project.controllers.AlbumController;
import com.example.music_project.models.Song;
import com.example.music_project.views.adapters.SongAdapter;

import java.util.ArrayList;
import java.util.List;

public class AlbumFragment extends Fragment {
    // Khai báo các hằng số cho album, nghệ sĩ và thể loại
    private static final String ARG_ALBUM_ID = "album_id";
    private static final String ARG_ALBUM_NAME = "album_name";
    private static final String ARG_ARTIST_NAME = "artist_name";
    private static final String ARG_GENRE_NAME = "genre_name";

    private AlbumController albumController;
    private RecyclerView rvSongs;
    private SongAdapter songAdapter;
    private List<Song> songList = new ArrayList<>();

    // Hàm newInstance để tạo Fragment và truyền tham số (albumId, albumName, artistName, genreName)
    public static AlbumFragment newInstance(int albumId, String albumName, String artistName, String genreName) {
        AlbumFragment fragment = new AlbumFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_ALBUM_ID, albumId);
        args.putString(ARG_ALBUM_NAME, albumName); // Truyền tên album
        args.putString(ARG_ARTIST_NAME, artistName); // Truyền tên nghệ sĩ
        args.putString(ARG_GENRE_NAME, genreName); // Truyền tên thể loại
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_album_detail, container, false);

        // Khởi tạo controller và RecyclerView
        albumController = new AlbumController(getContext());
        rvSongs = view.findViewById(R.id.rv_album_songs);
        rvSongs.setLayoutManager(new LinearLayoutManager(getContext()));
        songAdapter = new SongAdapter(songList, song -> Toast.makeText(getContext(), "Đã chọn: " + song.getTitle(), Toast.LENGTH_SHORT).show());
        rvSongs.setAdapter(songAdapter);

        // Lấy albumId, albumName, artistName và genreName từ arguments và hiển thị
        if (getArguments() != null) {
            int albumId = getArguments().getInt(ARG_ALBUM_ID);
            String albumName = getArguments().getString(ARG_ALBUM_NAME);
            String artistName = getArguments().getString(ARG_ARTIST_NAME);
            String genreName = getArguments().getString(ARG_GENRE_NAME);

            // Hiển thị tên album, nghệ sĩ và thể loại trong TextView
            TextView tvAlbumName = view.findViewById(R.id.tv_album_title);
            TextView tvArtist = view.findViewById(R.id.tv_album_artist);
            TextView tvGenre = view.findViewById(R.id.tv_album_genre);
            tvAlbumName.setText(albumName);
            tvArtist.setText(artistName); // Hiển thị dưới dạng "Nghệ sĩ"
            tvGenre.setText(genreName);

            // Tải danh sách bài hát
            loadSongsInAlbum(albumId);
        }

        // Khởi tạo SearchView
        SearchView searchView = view.findViewById(R.id.sv_album_search); // Thay đổi ID nếu cần
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                songAdapter.filter(newText); // Gọi phương thức filter từ adapter
                return true;
            }
        });

        return view;
    }

    // Phương thức tải danh sách bài hát trong album
    private void loadSongsInAlbum(int albumId) {
        albumController.getSongsInAlbum(albumId, new AlbumController.OnSongsLoadedListener() {
            @Override
            public void onSongsLoaded(List<Song> songs) {
                if (songs != null && !songs.isEmpty()) {
                    songList.clear();
                    songList.addAll(songs);
                    songAdapter.notifyDataSetChanged(); // Cập nhật adapter
                } else {
                    Toast.makeText(getContext(), "Không có bài hát nào trong album", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
