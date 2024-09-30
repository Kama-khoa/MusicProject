package com.example.music_project.views.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.music_project.R;
import com.example.music_project.controllers.SongController;
import com.example.music_project.views.adapters.SongAdapter;

public class HomeFragment extends Fragment {
    private RecyclerView rvRecentSongs, rvPopularSongs;
    private SongController songController;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        songController = new SongController(getContext());

        rvRecentSongs = view.findViewById(R.id.rv_recent_songs);
        rvPopularSongs = view.findViewById(R.id.rv_popular_songs);

        // Thiết lập LayoutManager cho RecyclerView
        rvRecentSongs.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        rvPopularSongs.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));

        loadRecentSongs();
        loadPopularSongs();

        return view;
    }

    private void loadRecentSongs() {
        songController.getRecentSongs(songs -> {
            if (songs != null && !songs.isEmpty()) {
                SongAdapter adapter = new SongAdapter(songs);
                rvRecentSongs.setAdapter(adapter);
            } else {

            }
        });
    }

    private void loadPopularSongs() {
        songController.getPopularSongs(songs -> {
            if (songs != null && !songs.isEmpty()) {
                SongAdapter adapter = new SongAdapter(songs);
                rvPopularSongs.setAdapter(adapter);
            } else {

            }
        });
    }
}
