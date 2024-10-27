package com.example.music_project.views.fragments;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
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
import android.widget.Toast;

import com.example.music_project.views.activities.SongActivity;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;

public class SongDialogFragment extends DialogFragment {
    private static final int PICK_AUDIO_REQUEST = 1;
    private static final int PICK_IMAGE_REQUEST = 2;// Mã yêu cầu cho tệp âm thanh

    private EditText etTitle;
    private Spinner spArtist, spAlbum, spGenre;
    private TextView tvDuration;
    private Button btnSave, btnCancel, btnDelete, btnUpload;

    private SongDialogListener listener;
    private Song song;
    private String audioFilePath;
    private ImageView img_path;
    private String coverImagePath;

    private List<Artist> artists;
    private List<Album> albums;
    private List<Genre> genres;
    private ActivityResultLauncher<Intent> pickImageLauncher;

    private long songDuration;


    private int songId; // Đổi sang kiểu int
    private String songName;
    private int artistId;
    private int albumId;
    private int genreId;


    private String file_path;
    private String image_path;

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
            songId = Integer.parseInt(getArguments().getString("songId")); // Chuyển String thành int
            songName = getArguments().getString("songName");
            artistId =  Integer.parseInt(getArguments().getString("artist"));
            genreId =  Integer.parseInt(getArguments().getString("genre"));
            file_path = getArguments().getString("filepath");
            image_path = getArguments().getString("imagepath");
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
        spGenre = view.findViewById(R.id.sp_genre);
        tvDuration = view.findViewById(R.id.tv_duration);
        btnSave = view.findViewById(R.id.btn_save);
        btnCancel = view.findViewById(R.id.btn_cancel);
        btnDelete = view.findViewById(R.id.btn_delete);
        btnUpload = view.findViewById(R.id.btn_upload);
        img_path = view.findViewById(R.id.iv_cover);

        img_path.setOnClickListener(v -> pickImage());

        if (artists != null && genres != null) {
            spArtist.setAdapter(new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, artists));
            spGenre.setAdapter(new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, genres));
        } else {
            Log.e("SongDialogFragment", "Danh sách nghệ sĩ, album hoặc thể loại không được khởi tạo!");
        }

        if (song != null) {
            etTitle.setText(song.getTitle());
            tvDuration.setText(String.valueOf(song.getDuration()));
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
                                        songDuration = Long.parseLong(durationStr); // Lưu độ dài bài hát vào biến
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

        pickImageLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        if (imageUri != null) {
                            coverImagePath = imageUri.toString();
                            img_path.setImageURI(imageUri); // Cập nhật hình ảnh hiển thị
                        }
                    }
                }
        );



        return view;
    }

    private void pickImage() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        pickImageLauncher.launch(intent);
    }

    public void setArtists(List<Artist> artists) {
        this.artists = artists;
    }


    public void setGenres(List<Genre> genres) {
        this.genres = genres;
    }


    private void populateSpinners() {
        if (this.artists != null) {
            ArrayAdapter<Artist> artistAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, artists);
            spArtist.setAdapter(artistAdapter);
        }

        if(this.genres != null) {
            ArrayAdapter<Genre> genreAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, genres);
            spGenre.setAdapter(genreAdapter);
        }
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
        if (btnUpload != null) {
            btnUpload.setText("Audio đã chọn");
        }
    }

    private void saveSong() {
        String title = etTitle.getText().toString().trim();
        Artist selectedArtist = (Artist) spArtist.getSelectedItem();
        Genre selectedGenre = (Genre) spGenre.getSelectedItem();

        // Kiểm tra thông tin nhập vào không được để trống
        if (title.isEmpty() || selectedArtist == null || selectedGenre == null) {
            Toast.makeText(getContext(), "Vui lòng điền đầy đủ thông tin!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (song == null) {
            song = new Song();
        }

        song.setTitle(title);
        song.setArtist_id(selectedArtist.getArtist_id());
        song.setGenre_id(selectedGenre.getGenre_id());
        song.setDuration((int) songDuration);
        if (audioFilePath != null) {
            song.setFile_path(audioFilePath);

            Log.e("SongDialogFragment", "link ảnh:" + audioFilePath);
        }

        if (coverImagePath != null) {
             song.setImg_path(coverImagePath); // Lưu đường dẫn hình ảnh vào bài hát

        }

        Log.d("SongDialogFragment", "Song ID: " + song.getSong_id());

        if (listener != null) {
            listener.onSongSaved(song);
        }

        dismiss();
    }



}
