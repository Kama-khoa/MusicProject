package com.example.music_project.views.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.music_project.R;
import com.example.music_project.controllers.SongController;
import com.example.music_project.database.AppDatabase;
import com.example.music_project.models.Song;
import com.example.music_project.views.adapters.SongAdapter;
import com.example.music_project.utils.FileUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class SongActivity extends AppCompatActivity {
    private static final int PICK_AUDIO_REQUEST = 1;

    private SongController songController;
    private EditText etTitle, etArtistId, etAlbumId, etGenreId, etDuration;
    private Button btnAdd, btnUpdate, btnDelete, btnUpload;
    private RecyclerView rvSongs;
    private SongAdapter songAdapter;
    private List<Song> songs = new ArrayList<>();
    private int selectedSongId = -1;
    private Uri selectedAudioUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_song);

        songController = new SongController(AppDatabase.getInstance(this).songDao());
        initViews();
        setupRecyclerView();
        loadSongs();

        btnAdd.setOnClickListener(v -> addSong());
        btnUpdate.setOnClickListener(v -> updateSong());
        btnDelete.setOnClickListener(v -> deleteSong());
        btnUpload.setOnClickListener(v -> selectAudioFile());
    }

    private void initViews() {
        etTitle = findViewById(R.id.et_title);
        etArtistId = findViewById(R.id.et_artist_id);
        etAlbumId = findViewById(R.id.et_album_id);
        etGenreId = findViewById(R.id.et_genre_id);
        etDuration = findViewById(R.id.et_duration);
        btnAdd = findViewById(R.id.btn_add);
        btnUpdate = findViewById(R.id.btn_update);
        btnDelete = findViewById(R.id.btn_delete);
        btnUpload = findViewById(R.id.btn_upload);
        rvSongs = findViewById(R.id.rv_songs);
    }

    private void setupRecyclerView() {
        songAdapter = new SongAdapter(songs, this::onSongSelected);
        rvSongs.setLayoutManager(new LinearLayoutManager(this));
        rvSongs.setAdapter(songAdapter);
    }

    private void loadSongs() {
        songController.getAllSongs(new SongController.Callback<List<Song>>() {
            @Override
            public void onSuccess(List<Song> result) {
                songs.clear();
                songs.addAll(result);
                runOnUiThread(() -> songAdapter.notifyDataSetChanged());
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> Toast.makeText(SongActivity.this, error, Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void selectAudioFile() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("audio/*");
        startActivityForResult(intent, PICK_AUDIO_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_AUDIO_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            selectedAudioUri = data.getData();
            Toast.makeText(this, "Đã chọn file âm thanh", Toast.LENGTH_SHORT).show();
        }
    }

    private void addSong() {
        Song song = createSongFromInput();
        if (song != null) {
            if (selectedAudioUri != null) {
                String filePath = saveAudioFile(selectedAudioUri);
                if (filePath != null) {
                    song.setFile_path(filePath);
                } else {
                    Toast.makeText(this, "Lỗi khi lưu file âm thanh", Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            songController.addSong(song, new SongController.Callback<Void>() {
                @Override
                public void onSuccess(Void result) {
                    runOnUiThread(() -> {
                        Toast.makeText(SongActivity.this, "Bài hát đã được thêm", Toast.LENGTH_SHORT).show();
                        clearInputFields();
                        loadSongs();
                        selectedAudioUri = null;
                    });
                }

                @Override
                public void onError(String error) {
                    runOnUiThread(() -> Toast.makeText(SongActivity.this, error, Toast.LENGTH_SHORT).show());
                }
            });
        }
    }

    private void updateSong() {
        if (selectedSongId == -1) {
            Toast.makeText(this, "Vui lòng chọn một bài hát để cập nhật", Toast.LENGTH_SHORT).show();
            return;
        }

        Song song = createSongFromInput();
        if (song != null) {
            song.setSong_id(selectedSongId);

            if (selectedAudioUri != null) {
                String filePath = saveAudioFile(selectedAudioUri);
                if (filePath != null) {
                    song.setFile_path(filePath);
                } else {
                    Toast.makeText(this, "Lỗi khi lưu file âm thanh", Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            songController.updateSong(song, new SongController.Callback<Void>() {
                @Override
                public void onSuccess(Void result) {
                    runOnUiThread(() -> {
                        Toast.makeText(SongActivity.this, "Bài hát đã được cập nhật", Toast.LENGTH_SHORT).show();
                        clearInputFields();
                        loadSongs();
                        selectedSongId = -1;
                        selectedAudioUri = null;
                    });
                }

                @Override
                public void onError(String error) {
                    runOnUiThread(() -> Toast.makeText(SongActivity.this, error, Toast.LENGTH_SHORT).show());
                }
            });
        }
    }

    private void deleteSong() {
        if (selectedSongId == -1) {
            Toast.makeText(this, "Vui lòng chọn một bài hát để xóa", Toast.LENGTH_SHORT).show();
            return;
        }

        songController.getSongById(selectedSongId, new SongController.Callback<Song>() {
            @Override
            public void onSuccess(Song song) {
                songController.deleteSong(song, new SongController.Callback<Void>() {
                    @Override
                    public void onSuccess(Void result) {
                        runOnUiThread(() -> {
                            Toast.makeText(SongActivity.this, "Bài hát đã được xóa", Toast.LENGTH_SHORT).show();
                            clearInputFields();
                            loadSongs();
                            selectedSongId = -1;
                            // Xóa file âm thanh nếu cần
                            if (song.getFile_path() != null && !song.getFile_path().isEmpty()) {
                                File audioFile = new File(song.getFile_path());
                                if (audioFile.exists()) {
                                    audioFile.delete();
                                }
                            }
                        });
                    }

                    @Override
                    public void onError(String error) {
                        runOnUiThread(() -> Toast.makeText(SongActivity.this, error, Toast.LENGTH_SHORT).show());
                    }
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> Toast.makeText(SongActivity.this, error, Toast.LENGTH_SHORT).show());
            }
        });
    }

    private Song createSongFromInput() {
        try {
            String title = etTitle.getText().toString();
            int artistId = Integer.parseInt(etArtistId.getText().toString());
            int albumId = Integer.parseInt(etAlbumId.getText().toString());
            int genreId = Integer.parseInt(etGenreId.getText().toString());
            int duration = Integer.parseInt(etDuration.getText().toString());

            Song song = new Song();
            song.setTitle(title);
            song.setArtist_id(artistId);
            song.setAlbum_id(albumId);
            song.setGenre_id(genreId);
            song.setDuration(duration);
            song.setRelease_date(new Date());

            return song;
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Vui lòng nhập đúng định dạng cho các trường số", Toast.LENGTH_SHORT).show();
            return null;
        }
    }

    private String saveAudioFile(Uri audioUri) {
        try {
            File destFile = new File(getFilesDir(), "audio_" + System.currentTimeMillis() + ".mp3");
            FileUtils.copyFile(this, audioUri, destFile);
            return destFile.getAbsolutePath();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void clearInputFields() {
        etTitle.setText("");
        etArtistId.setText("");
        etAlbumId.setText("");
        etGenreId.setText("");
        etDuration.setText("");
        selectedAudioUri = null;
    }

    private void onSongSelected(Song song) {
        selectedSongId = song.getSong_id();
        etTitle.setText(song.getTitle());
        etArtistId.setText(String.valueOf(song.getArtist_id()));
        etAlbumId.setText(String.valueOf(song.getAlbum_id()));
        etGenreId.setText(String.valueOf(song.getGenre_id()));
        etDuration.setText(String.valueOf(song.getDuration()));
        selectedAudioUri = null; // Reset selected audio when a song is selected
    }
}