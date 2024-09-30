package com.example.music_project.views.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.fragment.app.Fragment;
import android.widget.ImageButton;
import com.example.music_project.R;
import com.example.music_project.controllers.PlayerController;

public class PlayerControlsFragment extends Fragment {
    private PlayerController playerController;
    private ImageButton btnPlayPause, btnNext, btnPrevious;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_player_controls, container, false);

        initializeViews(view);
        setupListeners();

        return view;
    }

    private void initializeViews(View view) {
        playerController = new PlayerController(requireContext());
//        btnPlayPause = view.findViewById(R.id.btn_play_pause);
        btnNext = view.findViewById(R.id.btn_next);
        btnPrevious = view.findViewById(R.id.btn_previous);
    }

    private void setupListeners() {
        btnPlayPause.setOnClickListener(v -> togglePlayPause());
//        btnNext.setOnClickListener(v -> playerController.playNext());
//        btnPrevious.setOnClickListener(v -> playerController.playPrevious());
    }

    private void togglePlayPause() {
        if (playerController.isPlaying()) {
            playerController.pause();
            btnPlayPause.setImageResource(R.drawable.ic_play);
        } else {
            playerController.play();
            btnPlayPause.setImageResource(R.drawable.ic_pause);
        }
    }

    public void updatePlayPauseButton(boolean isPlaying) {
        btnPlayPause.setImageResource(isPlaying ? R.drawable.ic_pause : R.drawable.ic_play);
    }
}