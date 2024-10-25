package com.example.music_project.views.fragments;

import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.provider.OpenableColumns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import com.example.music_project.R;
import com.example.music_project.models.Song;
import com.example.music_project.models.Artist;
import com.example.music_project.models.Album;
import com.example.music_project.models.Genre;
import android.media.MediaMetadataRetriever;
import com.example.music_project.views.activities.SongActivity;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;

public class SongDialogFragment extends DialogFragment {
    private static final int PICK_AUDIO_REQUEST = 1; // Mã yêu cầu cho tệp âm thanh

    private EditText etTitle;
    private Spinner spArtist, spAlbum, spGenre;
    private TextView tvDuration;
    private Button btnSave, btnCancel, btnDelete, btnUpload;

    private SongDialogListener listener;
    private Song song;
    private String audioFilePath;

    private List<Artist> artists;
    private List<Album> albums;
    private List<Genre> genres;

    private ActivityResultLauncher<Intent> audioFileLauncher;

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

        audioFileLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == getActivity().RESULT_OK && result.getData() != null) {
                        Uri audioUri = result.getData().getData();
                        if (audioUri != null) {
                            audioFilePath = audioUri.toString();

                            // Khởi tạo MediaMetadataRetriever bên trong try
                            try (MediaMetadataRetriever mmr = new MediaMetadataRetriever();
                                 ParcelFileDescriptor pfd = getContext().getContentResolver().openFileDescriptor(audioUri, "r")) {

                                if (pfd != null) {
                                    mmr.setDataSource(pfd.getFileDescriptor()); // Thiết lập nguồn từ FileDescriptor

                                    // Lấy độ dài bài hát (đơn vị là microsecond)
                                    String durationStr = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
                                    if (durationStr != null) {
                                        long durationInMillis = Long.parseLong(durationStr);
                                        long durationInSeconds = durationInMillis / 1000;
                                        tvDuration.setText("Thời lượng: "+ String.format("%d:%02d", durationInSeconds / 60, durationInSeconds % 60));
                                    }
                                }

                            } catch (IOException e) {
                                e.printStackTrace();
                                tvDuration.setText("Không thể lấy độ dài bài hát");
                            }

                            if (btnUpload != null) {
                                btnUpload.setText(audioFilePath); // Hoặc sử dụng audioFilePath nếu bạn muốn hiển thị đường dẫn
                            }
                        }
                    }
                }
        );



        return view;
    }

    public void setArtists(List<Artist> artists) {
        this.artists = artists;
    }

    public void setAlbums(List<Album> albums) {
        this.albums = albums;
    }

    public void setGenres(List<Genre> genres) {
        this.genres = genres;
    }

    private void populateSpinners() {
        if (this.artists != null) {
            ArrayAdapter<Artist> artistAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, artists);
            spArtist.setAdapter(artistAdapter);
        }

        if (this.albums != null) {
            ArrayAdapter<Album> albumAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, albums);
            spAlbum.setAdapter(albumAdapter);
        }
        if(this.genres != null) {
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
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("audio/*");
        audioFileLauncher.launch(intent);
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

//    @Override
//    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        if (requestCode == PICK_AUDIO_REQUEST && resultCode == getActivity().RESULT_OK && data != null) {
//            Uri audioUri = data.getData();
//            if (audioUri != null) {
//                audioFilePath = audioUri.toString();
//                // Cập nhật UI để hiển thị rằng một tệp âm thanh đã được chọn
//                if (btnUpload != null) {
//                    btnUpload.setText("Audio đã chọn");
//                }
//            }
//        }
//    }


}
