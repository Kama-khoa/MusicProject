package com.example.music_project.views.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.music_project.R;
import com.example.music_project.models.Playlist;
import com.example.music_project.views.adapters.PlaylistAdapter;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class PlaylistFragment extends Fragment {

    private ListView lvPlaylists;
    private PlaylistAdapter adapter;
    private List<Playlist> playlistList;
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_playlist, container, false);

        lvPlaylists = view.findViewById(R.id.lv_playlists);

        // Khởi tạo dữ liệu playlist
        playlistList = new ArrayList<>();
        // Thay đổi ở đây để thêm userId, details và imageResource
//        playlistList.add(new Playlist(1, "Bài hát ưa thích")); // Thay đổi
//        playlistList.add(new Playlist(1, "Hot Hits Vietnam")); // Thay đổi
//        playlistList.add(new Playlist(1, "Thiên Hạ Nghe Gì")); // Thay đổi
        // Thêm các playlist khác nếu cần

        // Khởi tạo adapter và set cho ListView
        adapter = new PlaylistAdapter(getContext(), playlistList);
        lvPlaylists.setAdapter(adapter);

        return view;
    }
}
