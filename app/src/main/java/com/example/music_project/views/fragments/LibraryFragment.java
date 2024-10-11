package com.example.music_project.views.fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.music_project.R;
import com.example.music_project.controllers.AlbumController;
import com.example.music_project.controllers.GenreController;
import com.example.music_project.controllers.PlaylistController;
import com.example.music_project.controllers.UserController;
import com.example.music_project.database.AppDatabase;
import com.example.music_project.models.Album;
import com.example.music_project.models.Genre;
import com.example.music_project.models.Playlist;
import com.example.music_project.models.User;
import com.example.music_project.views.adapters.AlbumAdapter;
import com.example.music_project.views.adapters.PlaylistAdapter;
import com.example.music_project.views.adapters.SongAdapter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class LibraryFragment extends Fragment {
    private RecyclerView rvLibraryItems;
    private PlaylistController playlistController;
    private AlbumController albumController;
    private UserController userController;
    private GenreController genreController;

    private Button btnPlaylist, btnAlbum, btnArtist;
    private PlaylistAdapter playlistAdapter;
    private AlbumAdapter albumAdapter;
    private SongAdapter songAdapter;

    // Danh sách playlist và album
    private List<Playlist> playlistList = new ArrayList<>();
    private List<Album> albumList = new ArrayList<>();
    private List<Genre> genreList = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_library, container, false);

        // Khởi tạo controller và RecyclerView
        playlistController = new PlaylistController(getContext());
        albumController = new AlbumController(getContext());
        userController = new UserController(getContext());
        genreController = new GenreController(getContext());

        rvLibraryItems = view.findViewById(R.id.rv_library_items);
        rvLibraryItems.setLayoutManager(new LinearLayoutManager(getContext()));

        // Khởi tạo các nút
        btnPlaylist = view.findViewById(R.id.btn_playlist);
        btnAlbum = view.findViewById(R.id.btn_album);
        btnArtist = view.findViewById(R.id.btn_artist);

        ImageView ivAdd = view.findViewById(R.id.iv_add);

        // Xử lý khi nhấn nút thêm playlist
        ivAdd.setOnClickListener(v -> showAddMenu(v));

        // Thiết lập sự kiện cho các nút
        setupListeners();

        // Lấy User ID hiện tại và lưu vào SharedPreferences
        getCurrentUserId();

        return view;
    }

    // Hiển thị menu thêm playlist
    private void showAddMenu(View v) {
        PopupMenu popupMenu = new PopupMenu(getContext(), v);
        popupMenu.getMenuInflater().inflate(R.menu.menu_add_playlist, popupMenu.getMenu());

        popupMenu.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.add_playlist) {
                showAddPlaylistDialog();
                return true;
            }
            if (item.getItemId() == R.id.add_album) {
                showAddAlbumDialog();
                return true;
            }
            if (item.getItemId() == R.id.add_artist) {
                return true;
            }
            return false;
        });

        popupMenu.show();
    }

    // Lấy ID người dùng hiện tại và lưu vào SharedPreferences
    private void getCurrentUserId() {
        userController.getCurrentUser(new UserController.OnUserFetchedListener() {
            @Override
            public void onSuccess(User user) {
                long userId = user.getUser_id();
                saveUserId(userId);
                saveUserName(user.getUsername()); // Lưu tên người dùng
                Log.d("LibraryFragment", "User ID: " + userId);
            }

            @Override
            public void onFailure(String error) {
                Log.e("LibraryFragment", "Error fetching user: " + error);
                Toast.makeText(getContext(), "Lỗi: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }
    // Thêm phương thức để lưu tên người dùng vào SharedPreferences
    private void saveUserName(String userName) {
        SharedPreferences.Editor editor = getActivity().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE).edit();
        editor.putString("userName", userName);
        editor.apply();
        Log.d("LibraryFragment", "Saved User Name: " + userName);
    }

    // Lưu User ID vào SharedPreferences
    private void saveUserId(long userId) {
        SharedPreferences.Editor editor = getActivity().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE).edit();
        editor.putLong("userId", userId);
        editor.apply();
        Log.d("LibraryFragment", "Saved User ID: " + userId);
    }

    // Phương thức tải danh sách các playlist
    private void loadPlaylists() {
        playlistController.getPlaylists(new PlaylistController.OnPlaylistsLoadedListener() {
            @Override
            public void onPlaylistsLoaded(List<Playlist> playlists) {
                if (playlists == null || playlists.isEmpty()) {
                    Log.d("DEBUG", "Không có playlist nào");
                    return;
                }

                playlistList.clear();
                playlistList.addAll(playlists);

                // Lấy userName từ SharedPreferences
                SharedPreferences preferences = getActivity().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
                String userName = preferences.getString("userName", "Người dùng");

                // Cập nhật adapter
                if (playlistAdapter == null) {
                    playlistAdapter = new PlaylistAdapter(playlistList, playlist -> {
                        // Gọi loadSongsInPlaylist với đầy đủ tham số
                        loadSongsInPlaylist(playlist.getPlaylist_id(), playlist.getTitle(), userName);
                    });
                    rvLibraryItems.setAdapter(playlistAdapter);
                } else {
                    playlistAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(String error) {
                new Handler(Looper.getMainLooper()).post(() -> Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show());
            }
        });
    }

    // Phương thức tải danh sách bài hát trong playlist
// Phương thức tải bài hát trong playlist và chuyển sang màn hình chi tiết
    private void loadSongsInPlaylist(int playlistId, String playlistName, String userName) {
        DetailPlaylistFragment detailFragment = DetailPlaylistFragment.newInstance(playlistId, playlistName, userName);
        getActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, detailFragment)
                .addToBackStack(null)
                .commit();
    }

    // Tải danh sách album
    private void loadAlbums() {
        albumController.getAlbums(new AlbumController.OnAlbumsLoadedListener() {
            @Override
            public void onAlbumsLoaded(List<Album> albums) {
                if (albums == null || albums.isEmpty()) {
                    Log.d("DEBUG", "Không có album nào");
                    return;
                }

                // Cập nhật danh sách album
                albumList.clear();
                albumList.addAll(albums);
                // Cập nhật adapter
                if (albumAdapter == null) {
                    albumAdapter = new AlbumAdapter(albumList, album -> {
                        loadAlbumDetail( album.getAlbum_id(), album.getTitle(), String.valueOf(album.getArtist_id()), String.valueOf(album.getGenre_id()));
                    });
                    rvLibraryItems.setAdapter(albumAdapter);
                } else {
                    albumAdapter.notifyDataSetChanged(); // Cập nhật dữ liệu
                }
            }

            @Override
            public void onFailure(String error) {
                new Handler(Looper.getMainLooper()).post(() -> Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show());
            }
        });
    }


    // Thiết lập sự kiện cho các nút
    private void setupListeners() {
        btnPlaylist.setOnClickListener(view -> {
            loadPlaylists(); // Gọi phương thức để tải danh sách phát
            rvLibraryItems.setAdapter(playlistAdapter); // Đảm bảo cập nhật adapter cho RecyclerView
        });

        btnAlbum.setOnClickListener(view -> {
            loadAlbums();
            rvLibraryItems.setAdapter(albumAdapter); // Đảm bảo cập nhật adapter cho RecyclerView
        });

        btnArtist.setOnClickListener(v -> {
            // Cập nhật RecyclerView cho nghệ sĩ nếu có
        });
    }

    // Hiển thị dialog thêm playlist
    private void showAddPlaylistDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_add_playlist, null);
        builder.setView(dialogView);

        EditText edtPlaylistName = dialogView.findViewById(R.id.edt_playlist_name);

        builder.setTitle("Thêm Danh Sách Phát")
                .setPositiveButton("Tạo", (dialog, id) -> {
                    String playlistName = edtPlaylistName.getText().toString().trim();
                    if (!playlistName.isEmpty()) {
                        SharedPreferences preferences = getActivity().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
                        long userIdLong = preferences.getLong("userId", -1);
                        int userId = (int) userIdLong;

                        playlistController.createPlaylist(userId, playlistName, new PlaylistController.OnPlaylistCreatedListener() {
                            @Override
                            public void onSuccess() {
                                Toast.makeText(getContext(), "Danh sách phát " + playlistName + " được tạo", Toast.LENGTH_SHORT).show();
                                loadPlaylistsByUserID(userId);
                            }

                            @Override
                            public void onFailure(String error) {
                                Toast.makeText(getContext(), "Lỗi: " + error, Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        Toast.makeText(getContext(), "Tên danh sách phát không được để trống", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Hủy", (dialog, id) -> dialog.dismiss())
                .create()
                .show();
    }

    // Tải danh sách phát theo User ID
    private void loadPlaylistsByUserID(int userId) {
        playlistController.getPlaylistsByUserID(userId, new PlaylistController.OnPlaylistsLoadedListener() {
            @Override
            public void onPlaylistsLoaded(List<Playlist> playlists) {
                playlistList.clear();
                playlistList.addAll(playlists);
                if (playlistAdapter != null) {
                    playlistAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showAddAlbumDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_add_album, null);
        builder.setView(dialogView);

        EditText edtAlbumName = dialogView.findViewById(R.id.edt_album_name);
        Spinner spinnerGenre = dialogView.findViewById(R.id.spinner_genre);
        TextView tvReleaseDate = dialogView.findViewById(R.id.tv_release_date);

        // Lấy ngày hiện tại và hiển thị trong TextView
        String currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        tvReleaseDate.setText(currentDate);

        // Lấy danh sách genre từ database và hiển thị trong Spinner
        genreController.getAllGenres(new GenreController.OnGenresLoadedListener() {
            @Override
            public void onGenresLoaded(List<Genre> genres) {
                // Điền dữ liệu vào spinner trên UI thread
                getActivity().runOnUiThread(() -> {
                    if (spinnerGenre != null) {
                        ArrayAdapter<Genre> adapter = new ArrayAdapter<>(getContext(),
                                android.R.layout.simple_spinner_item, genres);
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinnerGenre.setAdapter(adapter);
                    } else {
                        Log.e("LibraryFragment", "Spinner is null");
                    }
                });
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(getContext(), "Không thể tải danh sách thể loại", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setTitle("Thêm Album Mới")
                .setPositiveButton("Tạo", (dialog, id) -> {
                    String albumName = edtAlbumName.getText().toString().trim();
                    Genre selectedGenre = (Genre) spinnerGenre.getSelectedItem();

                    if (!albumName.isEmpty() && selectedGenre != null) { // Kiểm tra selectedGenre không null

                        int genreId = selectedGenre.getGenre_id(); // Lấy genreId từ đối tượng Genre

                        // Lấy userId từ SharedPreferences làm artistId
                        SharedPreferences preferences = getActivity().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
                        long userIdLong = preferences.getLong("userId", -1);
                        int userId = (int) userIdLong;

                        // Tạo album với ngày phát hành hiện tại
                        Date releaseDate = new Date();
                        Album album = new Album(albumName, userId, genreId, releaseDate);

                        // Gọi hàm tạo album từ AlbumController
                        albumController.createAlbum(userId, album, new AlbumController.OnAlbumCreatedListener() {
                            @Override
                            public void onSuccess() {
                                new Handler(Looper.getMainLooper()).post(() -> {
                                    Toast.makeText(getContext(), "Album " + albumName + " được tạo", Toast.LENGTH_SHORT).show();
                                });
                                loadAlbumsByUserID(userId);
                            }

                            @Override
                            public void onFailure(String error) {
                                new Handler(Looper.getMainLooper()).post(() -> {
                                    Toast.makeText(getContext(), "Lỗi: " + error, Toast.LENGTH_SHORT).show();
                                });
                            }
                        });
                    } else {
                        new Handler(Looper.getMainLooper()).post(() -> {
                            Toast.makeText(getContext(), "Tên album và thể loại không được để trống", Toast.LENGTH_SHORT).show();
                        });
                    }
                })
                .setNegativeButton("Hủy", (dialog, id) -> dialog.dismiss())
                .create()
                .show();
    }

    private void loadAlbumsByUserID(int userId) {
        albumController.getAlbumsByUserID(userId, new AlbumController.OnAlbumsLoadedListener() {
            @Override
            public void onAlbumsLoaded(List<Album> albums) {
                albumList.clear();
                albumList.addAll(albums);
                if (albumAdapter != null) {
                    albumAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadAlbumDetail(int albumId, String albumName, String artistName, String genreName) {
        AlbumFragment albumDetailFragment = AlbumFragment.newInstance(albumId, albumName, artistName, genreName);
        getActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, albumDetailFragment)
                .addToBackStack(null)
                .commit();
    }


}
