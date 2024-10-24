package com.example.music_project.views.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
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
import java.util.List;

public class SongActivity extends AppCompatActivity implements SongDialogFragment.SongDialogListener {
    private static final int PICK_AUDIO_REQUEST = 1;

    private SongController songController;
    private Button btnAdd;
    private RecyclerView rvSongs;
    private SongAdapter songAdapter;
    private List<Song> songs = new ArrayList<>();
    private Uri selectedAudioUri;

    private List<Artist> artists = new ArrayList<>();
    private List<Album> albums = new ArrayList<>();
    private List<Genre> genres = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_song);

        songController = new SongController(AppDatabase.getInstance(this).songDao(),
                AppDatabase.getInstance(this).artistDao(),
                AppDatabase.getInstance(this).albumDao(),
                AppDatabase.getInstance(this).genreDao());
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

        songController.getAllArtists(new SongController.Callback<List<Artist>>() {
            @Override
            public void onSuccess(List<Artist> result) {
                artists.clear();
                artists.addAll(result);

                Log.d("ArtistData", "Số nghệ sĩ nhận được: " + result.size());
                for (Artist artist : result) {
                    Log.d("ArtistData", "Nghệ sĩ: " + artist.getArtist_name()); // Thay đổi getName() nếu cần
                }
            }

            @Override
            public void onError(String error) {
                Log.e("ArtistData", "Lỗi nhận nghệ sĩ: " + error);
            }
        });
    }

    private void showAddSongDialog() {
        // Gọi hàm để lấy danh sách nghệ sĩ
        getArtists(new Callback<List<Artist>>() {
            @Override
            public void onSuccess(List<Artist> artistsList) {
                artists.clear();
                artists.addAll(artistsList); // Gán danh sách nghệ sĩ

                // Gọi hàm để lấy danh sách album
                getAlbums(new Callback<List<Album>>() {
                    @Override
                    public void onSuccess(List<Album> albumsList) {
                        albums.clear();
                        albums.addAll(albumsList); // Gán danh sách album

                        // Gọi hàm để lấy danh sách thể loại
                        getGenres(new Callback<List<Genre>>() {
                            @Override
                            public void onSuccess(List<Genre> genresList) {
                                genres.clear();
                                genres.addAll(genresList); // Gán danh sách thể loại

                                // Kiểm tra nếu danh sách không rỗng
                                if (artists.isEmpty() || albums.isEmpty() || genres.isEmpty()) {
                                    Toast.makeText(SongActivity.this, "Không có dữ liệu nghệ sĩ, album hoặc thể loại", Toast.LENGTH_SHORT).show();
                                    return;
                                }

                                // Tạo và hiển thị SongDialogFragment
                                SongDialogFragment dialog = new SongDialogFragment();
                                dialog.setListener(SongActivity.this);
                                dialog.setArtists(artists);
                                dialog.setAlbums(albums);
                                dialog.setGenres(genres);
                                dialog.show(getSupportFragmentManager(), "SongDialog");
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

            @Override
            public void onError(String error) {
                runOnUiThread(() -> Toast.makeText(SongActivity.this, error, Toast.LENGTH_SHORT).show());
            }
        });
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

    public void getArtists(Callback<List<Artist>> callback) {
        songController.getAllArtists(new SongController.Callback<List<Artist>>() {
            @Override
            public void onSuccess(List<Artist> result) {
                callback.onSuccess(result); // Truyền danh sách nghệ sĩ vào callback
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> Toast.makeText(SongActivity.this, error, Toast.LENGTH_SHORT).show());
                callback.onError(error);
            }
        });
    }

    public void getAlbums(Callback<List<Album>> callback) {
        songController.getAllAlbums(new SongController.Callback<List<Album>>() {
            @Override
            public void onSuccess(List<Album> result) {
                callback.onSuccess(result); // Truyền danh sách album vào callback
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> Toast.makeText(SongActivity.this, error, Toast.LENGTH_SHORT).show());
                callback.onError(error);
            }
        });
    }

    public void getGenres(Callback<List<Genre>> callback) {
        songController.getAllGenres(new SongController.Callback<List<Genre>>() {
            @Override
            public void onSuccess(List<Genre> result) {
                callback.onSuccess(result); // Truyền danh sách thể loại vào callback
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> Toast.makeText(SongActivity.this, error, Toast.LENGTH_SHORT).show());
                callback.onError(error);
            }
        });
    }

    public interface Callback<T> {
        void onSuccess(T result);
        void onError(String error);
    }
}
