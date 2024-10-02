package com.example.music_project.views.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.music_project.R;
import com.example.music_project.controllers.SongController;
import com.example.music_project.database.AppDatabase;
import com.example.music_project.models.Song;
import com.example.music_project.models.Artist;
import com.example.music_project.models.Album;
import com.example.music_project.models.Genre;
import com.example.music_project.views.adapters.SongAdapter;
import com.example.music_project.utils.FileUtils;
import com.example.music_project.views.fragments.SongDialogFragment;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class SongActivity extends AppCompatActivity implements SongDialogFragment.SongDialogListener {
    private static final int PICK_AUDIO_REQUEST = 1;

    private SongController songController;
    private Button btnAdd;
    private RecyclerView rvSongs;
    private SongAdapter songAdapter;
    private List<Song> songs = new ArrayList<>();
    private Uri selectedAudioUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_song);

        songController = new SongController(AppDatabase.getInstance(this).songDao());
        initViews();
        setupRecyclerView();
        loadSongs();

        btnAdd.setOnClickListener(v -> showAddSongDialog());
    }

    private void initViews() {
        btnAdd = findViewById(R.id.btn_add);
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

    private void showAddSongDialog() {
        SongDialogFragment dialog = new SongDialogFragment();
        dialog.setListener(this);
        dialog.show(getSupportFragmentManager(), "SongDialog");
    }

    private void onSongSelected(Song song) {
        SongDialogFragment dialog = SongDialogFragment.newInstance(song);
        dialog.setListener(this);
        dialog.show(getSupportFragmentManager(), "SongDialog");
    }

    @Override
    public void onSongSaved(Song song) {
        if (song.getSong_id() == 0) {
            addSong(song);
        } else {
            updateSong(song);
        }
    }

    private void addSong(Song song) {
        songController.addSong(song, new SongController.Callback<Void>() {
            @Override
            public void onSuccess(Void result) {
                runOnUiThread(() -> {
                    Toast.makeText(SongActivity.this, "Bài hát đã được thêm", Toast.LENGTH_SHORT).show();
                    loadSongs();
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> Toast.makeText(SongActivity.this, error, Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void updateSong(Song song) {
        songController.updateSong(song, new SongController.Callback<Void>() {
            @Override
            public void onSuccess(Void result) {
                runOnUiThread(() -> {
                    Toast.makeText(SongActivity.this, "Bài hát đã được cập nhật", Toast.LENGTH_SHORT).show();
                    loadSongs();
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> Toast.makeText(SongActivity.this, error, Toast.LENGTH_SHORT).show());
            }
        });
    }

    @Override
    public void onSongDeleted(Song song) {
        songController.deleteSong(song, new SongController.Callback<Void>() {
            @Override
            public void onSuccess(Void result) {
                runOnUiThread(() -> {
                    Toast.makeText(SongActivity.this, "Bài hát đã được xóa", Toast.LENGTH_SHORT).show();
                    loadSongs();
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> Toast.makeText(SongActivity.this, error, Toast.LENGTH_SHORT).show());
            }
        });
    }

    @Override
    public void onAudioFileRequested() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("audio/*");
        startActivityForResult(intent, PICK_AUDIO_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_AUDIO_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            selectedAudioUri = data.getData();
            String filePath = saveAudioFile(selectedAudioUri);
            if (filePath != null) {
                SongDialogFragment dialog = (SongDialogFragment) getSupportFragmentManager().findFragmentByTag("SongDialog");
                if (dialog != null) {
                    dialog.setAudioFilePath(filePath);
                }
            } else {
                Toast.makeText(this, "Lỗi khi lưu file âm thanh", Toast.LENGTH_SHORT).show();
            }
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

    // Các phương thức này cần được triển khai để cung cấp dữ liệu cho SongDialogFragment
    public List<Artist> getArtists() {
        // TODO: Implement this method to fetch artists from your database
        return new ArrayList<>();
    }

    public List<Album> getAlbums() {
        // TODO: Implement this method to fetch albums from your database
        return new ArrayList<>();
    }

    public List<Genre> getGenres() {
        // TODO: Implement this method to fetch genres from your database
        return new ArrayList<>();
    }
}