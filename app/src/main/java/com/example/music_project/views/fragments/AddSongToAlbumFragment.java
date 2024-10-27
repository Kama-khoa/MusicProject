package com.example.music_project.views.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.music_project.R;
import com.example.music_project.controllers.SongController;
import com.example.music_project.database.AppDatabase;
import com.example.music_project.database.SongDao;
import com.example.music_project.models.Song;
import com.example.music_project.views.adapters.SongAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AddSongToAlbumFragment extends Fragment implements SongAdapter.OnSongClickListener {

    private RecyclerView rcvSongs;
    private SearchView searchView;
    private Button btnAddToAlbum;
    private SongAdapter songAdapter;
    private List<Song> songList = new ArrayList<>();
    private List<Song> fullSongList = new ArrayList<>();

    public static AddSongToAlbumFragment newInstance(int albumId) {
        AddSongToAlbumFragment fragment = new AddSongToAlbumFragment();
        Bundle args = new Bundle();
        args.putInt("albumId", albumId); // Set the albumId in arguments
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_song_to_album, container, false);
        searchView = view.findViewById(R.id.sv_song_search);
        btnAddToAlbum = view.findViewById(R.id.btn_add_to_album);
        rcvSongs = view.findViewById(R.id.rv_select_songs);
        rcvSongs.setLayoutManager(new LinearLayoutManager(getContext()));

        int albumId = getArguments().getInt("albumId", -1);
        // Load available songs for the album
        loadAvailableSongs(albumId);

        // Set up the adapter
        songAdapter = new SongAdapter(songList, this);
        rcvSongs.setAdapter(songAdapter);

        // Set up search functionality
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterSongs(newText);
                return true;
            }
        });

        // Add selected songs to the album
        btnAddToAlbum.setOnClickListener(v -> addSelectedSongsToAlbum());

        return view;
    }

    private void loadAvailableSongs(int albumId) {
        SongDao songDao = AppDatabase.getInstance(getContext()).songDao();
        SongController songController = new SongController(songDao);

        songController.getSongsInAlbum(albumId, new SongController.Callback<List<Song>>() {
            @Override
            public void onSuccess(List<Song> songsInAlbum) {
                List<Song> allSongsInAlbum = new ArrayList<>(songsInAlbum);

                songController.getAvailableAlbumSongs(albumId, new SongController.Callback<List<Song>>() {
                    @Override
                    public void onSuccess(List<Song> availableSongs) {
                        List<Song> filteredSongs = new ArrayList<>();
                        for (Song song : availableSongs) {
                            if (!allSongsInAlbum.contains(song)) {
                                filteredSongs.add(song);
                            }
                        }

                        fullSongList.clear();
                        fullSongList.addAll(filteredSongs);
                        songList.clear();
                        songList.addAll(filteredSongs);
                        songAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onError(String error) {
                        new Handler(Looper.getMainLooper()).post(() -> Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show());
                    }
                });
            }

            @Override
            public void onError(String error) {
                new Handler(Looper.getMainLooper()).post(() -> Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show());
            }
        });
    }


    private void filterSongs(String query) {
        List<Song> filteredList = new ArrayList<>();
        for (Song song : fullSongList) {
            if (song.getTitle().toLowerCase().contains(query.toLowerCase())) {
                filteredList.add(song);
            }
        }
        songList.clear();
        songList.addAll(filteredList);
        songAdapter.notifyDataSetChanged();
    }

    private void addSelectedSongsToAlbum() {
        List<Song> selectedSongs = songAdapter.getSelectedSongs();

        if (selectedSongs.isEmpty()) {
            new Handler(Looper.getMainLooper()).post(() -> Toast.makeText(getContext(), "Please select at least one song!", Toast.LENGTH_SHORT).show());
            return;
        }

        SongController songController = new SongController(AppDatabase.getInstance(getContext()).songDao());
        int albumId = getArguments().getInt("albumId",-1);

        songController.addSongsToAlbum(albumId, selectedSongs, new SongController.Callback<Void>() {
            @Override
            public void onSuccess(Void result) {
                new Handler(Looper.getMainLooper()).post(() -> Toast.makeText(getContext(), "Songs added to album successfully!", Toast.LENGTH_SHORT).show());
                requireActivity().getSupportFragmentManager().popBackStack();
            }

            @Override
            public void onError(String error) {
                new Handler(Looper.getMainLooper()).post(() -> Toast.makeText(getContext(), "Error: " + error, Toast.LENGTH_SHORT).show());
            }
        });
    }

    @Override
    public void onSongClick(Song song) {
        new Handler(Looper.getMainLooper()).post(() -> Toast.makeText(getContext(), "Clicked: " + song.getTitle(), Toast.LENGTH_SHORT).show());
    }
}
