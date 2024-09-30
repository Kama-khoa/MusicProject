package com.example.music_project.views.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import com.example.music_project.R;
import com.example.music_project.controllers.SongController;
import com.example.music_project.views.adapters.SongAdapter;

import java.util.List;

public class LibraryFragment extends Fragment {
    private RecyclerView rvLibrarySongs;
    private SongController songController;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_library, container, false);

        songController = new SongController(getContext());
//        rvLibrarySongs = view.findViewById(R.id.rv_library_songs);

        // Nếu bạn chưa set LayoutManager trong XML, bạn có thể set nó ở đây
        // rvLibrarySongs.setLayoutManager(new LinearLayoutManager(getContext()));

//        loadLibrarySongs();

        return view;
    }

//    private void loadLibrarySongs() {
//        songController.getAllSongs(songs -> {
//            SongAdapter adapter = new SongAdapter(songs);
//            rvLibrarySongs.setAdapter(adapter);
//        });
//    }
}
