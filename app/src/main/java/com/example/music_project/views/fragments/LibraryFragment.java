package com.example.music_project.views.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.music_project.R;
import com.example.music_project.controllers.SongController;
import com.example.music_project.views.adapters.SongAdapter;

import java.util.List;

public class LibraryFragment extends Fragment {
    private RecyclerView rvLibrarySongs;
    private SongController songController;
    private Button btnPlaylist, btnAlbum, btnArtist;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_library, container, false);

        // Khởi tạo songController và RecyclerView
       // songController = new SongController(getContext());
        //rvLibrarySongs = view.findViewById(R.id.rv_library_songs);
       // rvLibrarySongs.setLayoutManager(new LinearLayoutManager(getContext()));

        // Khởi tạo các nút
        btnPlaylist = view.findViewById(R.id.btn_playlist);
        btnAlbum = view.findViewById(R.id.btn_album);
        btnArtist = view.findViewById(R.id.btn_artist);

        // Thiết lập sự kiện cho các nút
        setupListeners();

        // Tải danh sách bài hát
       // loadLibrarySongs();

        return view;
    }
//    private void loadLibrarySongs() {
//        songController.getAllSongs(songs -> {
//            SongAdapter adapter = new SongAdapter(songs);
//            rvLibrarySongs.setAdapter(adapter);
//        });
//    }

    private void setupListeners() {
        btnPlaylist.setOnClickListener(v -> {
            // Chuyển sang PlaylistFragment
            PlaylistFragment playlistFragment = new PlaylistFragment();
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, playlistFragment)
                    .addToBackStack(null) // cho phép quay lại maàn hình trc đó nếu nhấn back
                    .commit();
        });   // thay tế fragment hiện tại bằng fragment playlist

        btnAlbum.setOnClickListener(v -> {
            // Chuyển sang AlbumFragment
            AlbumFragment albumFragment = new AlbumFragment();
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, albumFragment)
                    .addToBackStack(null)
                    .commit();
        });

        btnArtist.setOnClickListener(v -> {
            // Chuyển sang ArtistFragment
            ArtistFragment artistFragment = new ArtistFragment();
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, artistFragment)
                    .addToBackStack(null)
                    .commit();
        });
    }
}
