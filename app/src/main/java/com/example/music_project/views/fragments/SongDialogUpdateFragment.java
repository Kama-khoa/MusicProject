package com.example.music_project.views.fragments;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
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
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.bumptech.glide.Glide;
import com.example.music_project.R;
import com.example.music_project.models.Artist;
import com.example.music_project.models.Genre;
import com.example.music_project.models.Song;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;

public class SongDialogUpdateFragment extends DialogFragment {
    private static final int PICK_AUDIO_REQUEST = 1;
    private static final int PICK_IMAGE_REQUEST = 2;

    private EditText etTitle;
    private Spinner spArtist, spGenre;
    private TextView tvDuration;
    private Button btnSave, btnCancel, btnDelete, btnUpload;

    private SongDialogUpdateListener listener;
    private Song song;
    private String audioFilePath;
    private ImageView img_path;
    private String coverImagePath;

    private List<Artist> artists;
    private List<Genre> genres;

    private int songId;
    private String songName;
    private int artistId;
    private int genreId;
    private int duration;

    private long SongDuration;


    private String file_path;
    private String image_path;

    private ActivityResultLauncher<Intent> audioFileLauncher;
    private ActivityResultLauncher <Intent> pickImageLauncher;

    private static final int REQUEST_CODE_PICK_IMAGE = 1;

    public interface SongDialogUpdateListener {
        void onSongSavedtoUpdate(Song song);
        void onSongDeleted(Song song);
        void onAudioFileRequested();
    }

    public static SongDialogUpdateFragment newInstance(Song song) {
        SongDialogUpdateFragment fragment = new SongDialogUpdateFragment();
        Bundle args = new Bundle();
        args.putSerializable("song", (Serializable) song);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            try {
                songId = Integer.parseInt(getArguments().getString("songId", "-1"));
                songName = getArguments().getString("songName", "");
                artistId = Integer.parseInt(getArguments().getString("artist", "-1"));
                genreId = Integer.parseInt(getArguments().getString("genre", "-1"));
                duration = Integer.parseInt(getArguments().getString("duration", "-1"));
                file_path = getArguments().getString("filepath", "");
                image_path = getArguments().getString("imagepath", "");

                if (songId == -1 || artistId == -1 || genreId == -1 || duration == -1 ||
                        songName.isEmpty() || file_path.isEmpty() || image_path.isEmpty()) {

                    Toast.makeText(getContext(), "Thiếu thông tin bài hát hoặc thông tin không hợp lệ", Toast.LENGTH_LONG).show();
                }
            } catch (NumberFormatException e) {
                Toast.makeText(getContext(), "Thông tin không hợp lệ", Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(getContext(), "Không nhận được thông tin bài hát", Toast.LENGTH_LONG).show();
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
        View view = inflater.inflate(R.layout.dialog_update_song, container, false);

        etTitle = view.findViewById(R.id.et_ud_title);
        spArtist = view.findViewById(R.id.sp_ud_artist);
        spGenre = view.findViewById(R.id.sp_ud_genre);
        tvDuration = view.findViewById(R.id.tv_ud_duration);
        btnSave = view.findViewById(R.id.btn_ud_save);
        btnCancel = view.findViewById(R.id.btn_ud_cancel);
        btnDelete = view.findViewById(R.id.btn_ud_delete);

        img_path = view.findViewById(R.id.iv_ud_cover);

        Log.e("SongDialogFragment", "Tên bài hát" + songName);

        Log.e("SongDialogUpdateFragment", "link ảnh đc truyền : " +image_path);
        Log.e("SongDialogUpdateFragment", "tgian : " +String.format("Thời lượng: %d:%02d", duration / 60, duration % 60));

        img_path.setOnClickListener(v -> pickImage());

         loadCoverImage();
        if (artists != null && genres != null) {
            spArtist.setAdapter(new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, artists));
            spGenre.setAdapter(new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, genres));
        } else {
            Log.e("SongDialogFragment", "Danh sách nghệ sĩ, album hoặc thể loại không được khởi tạo!");
        }

        etTitle.setText(songName);
        tvDuration.setText(String.format("Thời lượng: %d:%02d", duration / 60, duration % 60));
        setSpinnerSelections();



        btnSave.setOnClickListener(v -> saveSongUpdate());
        btnCancel.setOnClickListener(v -> dismiss());
        btnDelete.setOnClickListener(v -> deleteSong());

        audioFileLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == getActivity().RESULT_OK && result.getData() != null) {
                        Uri audioUri = result.getData().getData();
                        if (audioUri != null) {
                            audioFilePath = audioUri.toString();
                            setAudioDuration(audioUri);
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
                            img_path.setImageURI(imageUri);
                        }
                    }
                }
        );
        return view;
    }






    private void setAudioDuration(Uri audioUri) {
        try (MediaMetadataRetriever mmr = new MediaMetadataRetriever();
             ParcelFileDescriptor pfd = getContext().getContentResolver().openFileDescriptor(audioUri, "r")) {

            if (pfd != null) {
                mmr.setDataSource(pfd.getFileDescriptor());
                String durationStr = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
                if (durationStr != null) {
                    SongDuration = Long.parseLong(durationStr);
                    long durationInMillis = Long.parseLong(durationStr);
                    long durationInSeconds = durationInMillis / 1000;
                    tvDuration.setText(String.format("Thời lượng: %d:%02d", durationInSeconds / 60, durationInSeconds % 60));
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
            tvDuration.setText("Không thể lấy độ dài bài hát");
        }
    }


    private void loadCoverImage() {
        String imagePath = image_path;

        if (imagePath != null && !imagePath.isEmpty()) {

            if (imagePath.startsWith("res/raw/")) {
                int resourceId = getResources().getIdentifier(
                        imagePath.replace("res/raw/", "").replace(".png", ""),
                        "raw",
                        getActivity().getPackageName());

                if (resourceId != 0) {
                    Glide.with(this)
                            .load(resourceId)
                            .into(img_path);
                } else {
                    img_path.setImageResource(R.drawable.ic_image_playlist);
                }
            }
            else if (imagePath.startsWith("content://")) {
                Uri imageUri = Uri.parse(imagePath);
                Glide.with(this)
                        .load(imageUri)
                        .into(img_path);
            }
            else {
                Glide.with(this)
                        .load(imagePath)
                        .into(img_path);
            }
        } else {
            img_path.setImageResource(R.drawable.ic_image_playlist);
        }
    }


    private void setSpinnerSelections() {
        if (artists != null) {
            for (int i = 0; i < artists.size(); i++) {
                if (artists.get(i).getArtist_id() == artistId) {
                    spArtist.setSelection(i);
                    break;
                }
            }
        }

        if (genres != null) {
            for (int i = 0; i < genres.size(); i++) {
                if (genres.get(i).getGenre_id() == genreId) {
                    spGenre.setSelection(i);
                    break;
                }
            }
        }
    }
    private void pickImage() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("image/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
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

    public void setListener(SongDialogUpdateListener listener) {
        this.listener = listener;
    }

    public void setAudioFilePath(String filePath) {
        this.audioFilePath = filePath;

        if (btnUpload != null) {
            btnUpload.setText("Audio đã chọn");
        }
    }

    private void saveSongUpdate() {
        String title = etTitle.getText().toString().trim();
        Artist selectedArtist = (Artist) spArtist.getSelectedItem();

        Genre selectedGenre = (Genre) spGenre.getSelectedItem();
        if (title.isEmpty() || selectedArtist == null || selectedGenre == null) {
            Toast.makeText(getContext(), "Vui lòng điền đầy đủ thông tin!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (song == null) {
            song = new Song();
        }

        Log.d("SongDialogUpdate", "Thay đổi tên mới : "+title);


        song.setTitle(title);
        song.setArtist_id(selectedArtist.getArtist_id());
        song.setGenre_id(selectedGenre.getGenre_id());
        song.setDuration((int) SongDuration);
        song.setFile_path(audioFilePath != null ? audioFilePath : file_path);
        //song.setImage_path(coverImagePath != null ? coverImagePath : image_path);

        Log.d("SongDialogUpdate", "Title: " + title);
        Log.d("SongDialogUpdate", "Artist ID: " + (selectedArtist != null ? selectedArtist.getArtist_id() : "null"));
        Log.d("SongDialogUpdate", "Genre ID: " + (selectedGenre != null ? selectedGenre.getGenre_id() : "null"));
        Log.d("SongDialogUpdate", "Duration: " + SongDuration);
        Log.d("SongDialogUpdate", "Audio File Path: " + (audioFilePath != null ? audioFilePath : "null"));
        Log.d("SongDialogUpdate", "Image File Path: " + (coverImagePath != null ? coverImagePath : "null"));

        if (listener != null) {
            listener.onSongSavedtoUpdate(song);
        } else {
            Log.e("SongDialogUpdate", "Listener is null!");
        }

        dismiss();
    }



}
