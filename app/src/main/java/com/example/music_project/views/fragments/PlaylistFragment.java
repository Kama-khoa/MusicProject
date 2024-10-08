package com.example.music_project.views.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.music_project.R;
import com.example.music_project.models.Playlist;
import com.example.music_project.views.adapters.PlaylistAdapter;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class PlaylistFragment extends Fragment {

    private RecyclerView rvPlaylists; // Thay ListView thành RecyclerView
    private PlaylistAdapter adapter;
    private List<Playlist> playlistList;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_playlist, container, false);

        rvPlaylists = view.findViewById(R.id.rv_playlists); // Cập nhật ID cho RecyclerView

        // Khởi tạo dữ liệu playlist
        playlistList = new ArrayList<>();
//        playlistList.add(new Playlist(1, "Bài hát ưa thích"));
//        playlistList.add(new Playlist(2, "Hot Hits Vietnam"));
//        playlistList.add(new Playlist(3, "Thiên Hạ Nghe Gì"));

        // Khởi tạo adapter và set cho RecyclerView
        adapter = new PlaylistAdapter(playlistList, new PlaylistAdapter.OnPlaylistClickListener() {
            @Override
            public void onPlaylistClick(Playlist playlist) {
                // Xử lý sự kiện nhấp vào playlist ở đây
                // Ví dụ: mở một Activity mới hoặc hiển thị chi tiết của playlist
            }
        });

        rvPlaylists.setLayoutManager(new LinearLayoutManager(getContext()));
        rvPlaylists.setAdapter(adapter);

        return view;
    }
}