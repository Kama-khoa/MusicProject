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
import com.example.music_project.controllers.PlaylistController;
import com.example.music_project.models.Song;
import com.example.music_project.views.adapters.SongAdapter;

import java.util.ArrayList;
import java.util.List;

public class DetailPlaylistFragment extends Fragment {
    // Khai báo các hằng số cho tên playlist và tên người dùng
    private static final String ARG_PLAYLIST_ID = "playlist_id";
    private static final String ARG_PLAYLIST_NAME = "playlist_name";
    private static final String ARG_USER_NAME = "user_name";

    private PlaylistController playlistController;
    private RecyclerView rvSongs;
    private SongAdapter songAdapter;
    private List<Song> songList = new ArrayList<>();

    // Hàm newInstance để tạo Fragment và truyền tham số (playlistId, playlistName, userName)
    public static DetailPlaylistFragment newInstance(int playlistId, String playlistName, String userName) {
        DetailPlaylistFragment fragment = new DetailPlaylistFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_PLAYLIST_ID, playlistId);
        args.putString(ARG_PLAYLIST_NAME, playlistName); // Truyền tên playlist
        args.putString(ARG_USER_NAME, userName); // Truyền tên người dùng
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_playlist_detail, container, false);

        // Khởi tạo controller và RecyclerView
        playlistController = new PlaylistController(getContext());
        rvSongs = view.findViewById(R.id.rv_songs);
        rvSongs.setLayoutManager(new LinearLayoutManager(getContext()));
        songAdapter = new SongAdapter(songList, song -> Toast.makeText(getContext(), "Đã chọn: " + song.getTitle(), Toast.LENGTH_SHORT).show());
        rvSongs.setAdapter(songAdapter);

        // Lấy playlistId, playlistName và userName từ arguments và hiển thị
        if (getArguments() != null) {
            int playlistId = getArguments().getInt(ARG_PLAYLIST_ID);
            String playlistName = getArguments().getString(ARG_PLAYLIST_NAME);
            String userName = getArguments().getString(ARG_USER_NAME);

            // Hiển thị tên playlist và tên người dùng trong TextView
            TextView tvPlaylistName = view.findViewById(R.id.tv_playlist_name);
            TextView tvUserName = view.findViewById(R.id.tv_playlist_user_name);
            tvPlaylistName.setText(playlistName);
            tvUserName.setText(userName);

            // Tải danh sách bài hát
            loadSongsInPlaylist(playlistId);
        }

        // Khởi tạo SearchView
        SearchView searchView = view.findViewById(R.id.sv_search); // Thay đổi ID nếu cần
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

    // Phương thức tải danh sách bài hát trong playlist
    private void loadSongsInPlaylist(int playlistId) {
        playlistController.getSongsInPlaylist(playlistId, new PlaylistController.OnSongsLoadedListener() {
            @Override
            public void onSongsLoaded(List<Song> songs) {
                if (songs != null && !songs.isEmpty()) {
                    songList.clear();
                    songList.addAll(songs);
                    songAdapter.notifyDataSetChanged(); // Cập nhật adapter
                } else {
                    Toast.makeText(getContext(), "Không có bài hát nào trong playlist", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
