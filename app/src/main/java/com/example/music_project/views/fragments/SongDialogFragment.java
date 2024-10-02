package com.example.music_project.views.fragments;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import com.example.music_project.R;
import com.example.music_project.models.Song;
import com.example.music_project.models.Artist;
import com.example.music_project.models.Album;
import com.example.music_project.models.Genre;
import com.example.music_project.views.activities.SongActivity;

import java.io.Serializable;
import java.util.List;

public class SongDialogFragment extends DialogFragment {
    private EditText etTitle;
    private Spinner spArtist, spAlbum, spGenre;
    private TextView tvDuration;
    private Button btnSave, btnCancel, btnDelete, btnUpload;

    private SongDialogListener listener;
    private Song song;
    private String audioFilePath;

    public interface SongDialogListener {
        void onSongSaved(Song song);
        void onSongDeleted(Song song);
        void onAudioFileRequested();
    }

    public static SongDialogFragment newInstance(Song song) {
        SongDialogFragment fragment = new SongDialogFragment();
        Bundle args = new Bundle();
        args.putSerializable("song", (Serializable) song);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            song = (Song) getArguments().getSerializable("song");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        return super.onCreateDialog(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_song, container, false);

        etTitle = view.findViewById(R.id.et_title);
        spArtist = view.findViewById(R.id.sp_artist);
        spAlbum = view.findViewById(R.id.sp_album);
        spGenre = view.findViewById(R.id.sp_genre);
        tvDuration = view.findViewById(R.id.tv_duration);
        btnSave = view.findViewById(R.id.btn_save);
        btnCancel = view.findViewById(R.id.btn_cancel);
        btnDelete = view.findViewById(R.id.btn_delete);
        btnUpload = view.findViewById(R.id.btn_upload);

        populateSpinners();

        if (song != null) {
            etTitle.setText(song.getTitle());
            tvDuration.setText(String.valueOf(song.getDuration()));
            // Set selected items for spinners
            // You'll need to find the correct position for each spinner based on the song's data
        }

        btnSave.setOnClickListener(v -> saveSong());
        btnCancel.setOnClickListener(v -> dismiss());
        btnDelete.setOnClickListener(v -> deleteSong());
        btnUpload.setOnClickListener(v -> requestAudioFile());

        return view;
    }

    private void populateSpinners() {
        SongActivity activity = (SongActivity) getActivity();
        if (activity != null) {
            List<Artist> artists = activity.getArtists();
            List<Album> albums = activity.getAlbums();
            List<Genre> genres = activity.getGenres();

            ArrayAdapter<Artist> artistAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, artists);
            spArtist.setAdapter(artistAdapter);

            ArrayAdapter<Album> albumAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, albums);
            spAlbum.setAdapter(albumAdapter);

            ArrayAdapter<Genre> genreAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, genres);
            spGenre.setAdapter(genreAdapter);
        }
    }

    private void saveSong() {
        String title = etTitle.getText().toString();
        Artist selectedArtist = (Artist) spArtist.getSelectedItem();
        Album selectedAlbum = (Album) spAlbum.getSelectedItem();
        Genre selectedGenre = (Genre) spGenre.getSelectedItem();

        if (song == null) {
            song = new Song();
        }

        song.setTitle(title);
        song.setArtist_id(selectedArtist.getArtist_id());
        song.setAlbum_id(selectedAlbum.getAlbum_id());
        song.setGenre_id(selectedGenre.getGenre_id());
        if (audioFilePath != null) {
            song.setFile_path(audioFilePath);
        }

        if (listener != null) {
            listener.onSongSaved(song);
        }

        dismiss();
    }

    private void deleteSong() {
        if (song != null && listener != null) {
            listener.onSongDeleted(song);
        }
        dismiss();
    }

    private void requestAudioFile() {
        if (listener != null) {
            listener.onAudioFileRequested();
        }
    }

    public void setListener(SongDialogListener listener) {
        this.listener = listener;
    }

    public void setAudioFilePath(String filePath) {
        this.audioFilePath = filePath;
        // Update UI to show that an audio file has been selected
        if (btnUpload != null) {
            btnUpload.setText("Audio đã chọn");
        }
    }
}