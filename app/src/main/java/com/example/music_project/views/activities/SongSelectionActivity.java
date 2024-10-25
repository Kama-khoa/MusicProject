package com.example.music_project.views.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.music_project.R;
import com.example.music_project.controllers.SongController;
import com.example.music_project.database.AppDatabase;
import com.example.music_project.database.SongDao;
import com.example.music_project.models.Song;
import com.example.music_project.views.adapters.SongAdapter;

import java.util.ArrayList;
import java.util.List;

public class SongSelectionActivity extends AppCompatActivity implements SongAdapter.OnSongClickListener {

    private RecyclerView rcvSongs;
    private SearchView searchView;
    private Button btnAddToPlaylist;
    private SongAdapter songAdapter;
    private List<Song> songList = new ArrayList<>();
    private List<Song> fullSongList = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playlist_song);
        searchView = findViewById(R.id.search_view);
        btnAddToPlaylist = findViewById(R.id.btn_add_to_playlist);
        rcvSongs = findViewById(R.id.rcv_songs);
        rcvSongs.setLayoutManager(new LinearLayoutManager(this));

        int playlistId = getIntent().getIntExtra("PLAYLIST_ID", -1); // Change "PLAYLIST_ID" to your actual key

        // Tải danh sách bài hát không có trong playlist
        loadAvailableSongs(playlistId); // Pass the playlistId here


        // Thiết lập adapter cho ListView
        songAdapter = new SongAdapter(songList, this); // Sửa tại đây
        rcvSongs.setAdapter(songAdapter);

        // Thiết lập tìm kiếm
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterSongs(newText);
                return true;
            }
        });

        // Thêm bài hát vào playlist
        btnAddToPlaylist.setOnClickListener(v -> {
            addSelectedSongsToPlaylist();
        });
    }

    private void loadAvailableSongs(int playlistId) {
        SongDao songDao = AppDatabase.getInstance(this).songDao();
        SongController songController = new SongController(songDao);

        // Lấy danh sách bài hát trong playlist
        songController.getSongsInPlaylist(playlistId, new SongController.Callback<List<Song>>() {
            @Override
            public void onSuccess(List<Song> songsInPlaylist) {
                // Lưu bài hát trong playlist vào một mảng
                List<Song> allSongsInPlaylist = new ArrayList<>(songsInPlaylist);

                // Lấy danh sách bài hát có sẵn
                songController.getAvailableSongs(playlistId, new SongController.Callback<List<Song>>() { // Gọi với playlistId
                    @Override
                    public void onSuccess(List<Song> availableSongs) {
                        // Lọc các bài hát không có trong playlist hiện tại
                        List<Song> filteredSongs = new ArrayList<>();
                        for (Song song : availableSongs) {
                            if (!allSongsInPlaylist.contains(song)) {
                                filteredSongs.add(song);
                            }
                        }

                        // Cập nhật danh sách bài hát hiển thị
                        fullSongList.clear();
                        fullSongList.addAll(filteredSongs);
                        songList.clear();
                        songList.addAll(filteredSongs);
                        songAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onError(String error) {
                        Toast.makeText(SongSelectionActivity.this, "Lỗi: " + error, Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onError(String error) {
                Toast.makeText(SongSelectionActivity.this, "Lỗi: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void filterSongs(String query) {
        List<Song> filteredList = new ArrayList<>();
        for (Song song : fullSongList) {
            if (song.getTitle().toLowerCase().contains(query.toLowerCase())) {
                filteredList.add(song);
            }
        }
        songList.clear();
        songList.addAll(filteredList);
        songAdapter.notifyDataSetChanged();
    }

    private void addSelectedSongsToPlaylist() {
        List<Song> selectedSongs = songAdapter.getSelectedSongs();

        if (selectedSongs.isEmpty()) {
            Toast.makeText(this, "Vui lòng chọn ít nhất một bài hát!", Toast.LENGTH_SHORT).show();
            return; // Thoát nếu không có bài hát nào được chọn
        }

        // Gọi phương thức trong controller để thêm bài hát vào playlist
        SongController songController = new SongController(AppDatabase.getInstance(this).songDao());
        int playlistId = getIntent().getIntExtra("PLAYLIST_ID", -1);

        songController.addSongsToPlaylist(playlistId, selectedSongs, new SongController.Callback<Void>() {
            @Override
            public void onSuccess(Void result) {
                runOnUiThread(() -> {
                    Toast.makeText(SongSelectionActivity.this, "Đã thêm bài hát vào playlist!", Toast.LENGTH_SHORT).show();
                    finish();
                });

            }

            @Override
            public void onError(String error) {
                //Toast.makeText(SongSelectionActivity.this, "Lỗi: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onSongClick(Song song) {
        // Xử lý khi bài hát được nhấp (nếu cần)
        Toast.makeText(this, "Clicked: " + song.getTitle(), Toast.LENGTH_SHORT).show();

    }
}