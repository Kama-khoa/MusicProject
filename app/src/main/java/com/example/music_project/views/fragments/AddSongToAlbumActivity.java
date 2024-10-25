//package com.example.music_project.views.fragments;
//
//import android.content.Intent;
//import android.os.Bundle;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.Button;
//import android.widget.SearchView;
//import android.widget.Toast;
//
//import androidx.annotation.NonNull;
//import androidx.annotation.Nullable;
//import androidx.fragment.app.Fragment;
//import androidx.recyclerview.widget.LinearLayoutManager;
//import androidx.recyclerview.widget.RecyclerView;
//
//import com.example.music_project.R;
//import com.example.music_project.controllers.SongController;
//import com.example.music_project.database.AlbumSongDao;
//import com.example.music_project.database.AppDatabase;
//import com.example.music_project.database.SongDao;
//import com.example.music_project.models.Song;
//import com.example.music_project.views.adapters.SongAdapter;
//import com.example.music_project.views.activities.AddSongActivity;
//
//import java.util.ArrayList;
//import java.util.List;
//
//public class AddSongToAlbumFragment extends Fragment {
//
//    private SearchView svSongSearch;
//    private RecyclerView rvSelectSongs;
//    private Button btnFilterArtist, btnFilterGenre, btnAddNewSong, btnAddToAlbum;
//    private SongAdapter songAdapter;
//    private List<Song> songList = new ArrayList<>();
//    private SongController songController;
//    private AppDatabase database;
//    private int albumId;  // Pass the albumId to the fragment
//
//    @Nullable
//    @Override
//    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
//        View view = inflater.inflate(R.layout.fragment_add_song_to_album, container, false);
//
//        // Initialize views
//        svSongSearch = view.findViewById(R.id.sv_song_search);
//        rvSelectSongs = view.findViewById(R.id.rv_select_songs);
//        btnFilterArtist = view.findViewById(R.id.btn_filter_artist);
//        btnFilterGenre = view.findViewById(R.id.btn_filter_genre);
//        btnAddNewSong = view.findViewById(R.id.btn_add_new_song);
//        btnAddToAlbum = view.findViewById(R.id.btn_add_to_album);
//
//        // Initialize AppDatabase and DAOs
//        database = AppDatabase.getInstance(getContext());
//        SongDao songDao = database.songDao();
//        AlbumSongDao albumSongDao = database.albumSongDao();
//
//        // Pass the DAOs to the SongController
//        songController = new SongController(songDao, albumSongDao);
//
//        // Set up the RecyclerView
//        rvSelectSongs.setLayoutManager(new LinearLayoutManager(getContext()));
//        songAdapter = new SongAdapter(songList, song -> {
//            // Handle song selection (toggle between selected/not selected)
//            song.setSelected(!song.isSelected());
//            songAdapter.notifyDataSetChanged();
//        });
//        rvSelectSongs.setAdapter(songAdapter);
//
//        // Load songs for the album
//        loadSongsForAlbum(albumId);
//
//        // Set up SearchView
//        svSongSearch.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
//            @Override
//            public boolean onQueryTextSubmit(String query) {
//                return false;
//            }
//
//            @Override
//            public boolean onQueryTextChange(String newText) {
//                songAdapter.filter(newText); // Filter the list based on the search query
//                return true;
//            }
//        });
//
//        // Other button setups...
//
//        return view;
//    }
//
//    // Method to load songs for the album
//    private void loadSongsForAlbum(int albumId) {
//        songController.getSongsForAlbum(albumId, new SongController.OnSongsLoadedListener() {
//            @Override
//            public void onSongsLoaded(List<Song> songs) {
//                if (songs != null && !songs.isEmpty()) {
//                    songList.clear();
//                    songList.addAll(songs);
//                    songAdapter.notifyDataSetChanged();
//                } else {
//                    Toast.makeText(getContext(), "Không có bài hát nào", Toast.LENGTH_SHORT).show();
//                }
//            }
//
//            @Override
//            public void onFailure(String error) {
//                Toast.makeText(getContext(), "Lỗi khi tải bài hát", Toast.LENGTH_SHORT).show();
//            }
//        });
//    }
//
//    // Pass the albumId from the activity/fragment when creating the fragment instance
//    public static AddSongToAlbumFragment newInstance(int albumId) {
//        AddSongToAlbumFragment fragment = new AddSongToAlbumFragment();
//        Bundle args = new Bundle();
//        args.putInt("albumId", albumId);
//        fragment.setArguments(args);
//        return fragment;
//    }
//
//    @Override
//    public void onCreate(@Nullable Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        if (getArguments() != null) {
//            albumId = getArguments().getInt("albumId");
//        }
//    }
//}}