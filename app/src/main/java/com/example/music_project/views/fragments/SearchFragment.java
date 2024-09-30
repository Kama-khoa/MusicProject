package com.example.music_project.views.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Button;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import com.example.music_project.R;
import com.example.music_project.controllers.SongController;
import com.example.music_project.views.adapters.SongAdapter;

import java.util.List;

public class SearchFragment extends Fragment {
    private EditText etSearch;
    private Button btnSearch;
    private RecyclerView rvSearchResults;
    private SongController songController;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);

        songController = new SongController(getContext());
        etSearch = view.findViewById(R.id.et_search);
        btnSearch = view.findViewById(R.id.btn_search);
        rvSearchResults = view.findViewById(R.id.rv_search_results);

        btnSearch.setOnClickListener(v -> searchSongs());

        return view;
    }

    private void searchSongs() {
        String query = etSearch.getText().toString();
        songController.searchSongs(query, songs -> {
            SongAdapter adapter = new SongAdapter(songs);
            rvSearchResults.setAdapter(adapter);
        });
    }
}
