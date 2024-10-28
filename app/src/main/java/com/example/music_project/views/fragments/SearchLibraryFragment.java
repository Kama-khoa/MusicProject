package com.example.music_project.views.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.music_project.R;
import com.example.music_project.controllers.AlbumController;
import com.example.music_project.controllers.PlaylistController;
import com.example.music_project.models.Album;
import com.example.music_project.models.Playlist;
import com.example.music_project.views.adapters.AlbumAdapter;
import com.example.music_project.views.adapters.PlaylistAdapter;

import java.util.ArrayList;
import java.util.List;

public class SearchLibraryFragment extends Fragment {
    private SearchView searchView;
    private Button btnCancel;
    private RecyclerView rvSearchResults;
    private AlbumController albumController;
    private PlaylistController playlistController;

    private AlbumAdapter albumAdapter;
    private PlaylistAdapter playlistAdapter;

    private List<Album> albumList = new ArrayList<>();
    private List<Playlist> playlistList = new ArrayList<>();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search_library, container, false);

        searchView = view.findViewById(R.id.search_view);
        btnCancel = view.findViewById(R.id.btn_cancel_search);
        rvSearchResults = view.findViewById(R.id.rv_library_items);
        rvSearchResults.setLayoutManager(new LinearLayoutManager(getContext()));

        albumController = new AlbumController(getContext());
        playlistController = new PlaylistController(getContext());

        setupListeners();
        return view;
    }

    private void setupListeners() {
        btnCancel.setOnClickListener(v -> getActivity().onBackPressed());

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.isEmpty()) {
                    clearSearchResults();
                } else {
                    searchPlaylists(newText);
                }
                return true;
            }
        });
    }

    private void searchPlaylists(String query) {

        playlistController.searchPlaylists(query, new PlaylistController.OnPlaylistsLoadedListener() {
            @Override
            public void onPlaylistsLoaded(List<Playlist> playlists) {
                Log.d("SearchFragment", "Playlists loaded: " + playlists.size());
                if (playlists != null && !playlists.isEmpty()) {
                    playlistList.clear();
                    playlistList.addAll(playlists);
                    if (playlistAdapter == null) {
                        playlistAdapter = new PlaylistAdapter(playlistList, playlist -> {
                            openPlaylistDetails(playlist);
                        });
                        rvSearchResults.setAdapter(playlistAdapter);
                    } else {
                        playlistAdapter.notifyDataSetChanged();
                    }
                } else {
                    Log.d("SearchFragment", "No playlists found.");
                }
            }

            @Override
            public void onFailure(String error) {
                showToast(error);
            }
        });
    }


    private void clearSearchResults() {
        albumList.clear();
        playlistList.clear();
        if (albumAdapter != null) {
            albumAdapter.notifyDataSetChanged();
        }
        if (playlistAdapter != null) {
            playlistAdapter.notifyDataSetChanged();
        }
    }

    private void openPlaylistDetails(Playlist playlist) {

        DetailPlaylistFragment detailPlaylistFragment = DetailPlaylistFragment.newInstance(playlist.getPlaylist_id(), playlist.getTitle(), "");
        getActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, detailPlaylistFragment)
                .addToBackStack(null)
                .commit();
    }

    private void showToast(String message) {
        new Handler(Looper.getMainLooper()).post(() -> Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show());
    }
}
