package com.example.music_project.views.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.music_project.R;
import com.example.music_project.controllers.AlbumController;
import com.example.music_project.controllers.ArtistController;
import com.example.music_project.controllers.GenreController;
import com.example.music_project.controllers.PlaylistController;
import com.example.music_project.controllers.UserController;
import com.example.music_project.models.Album;
import com.example.music_project.models.AlbumWithDetails;
import com.example.music_project.models.Artist;
import com.example.music_project.models.Genre;
import com.example.music_project.models.Artist;
import com.example.music_project.models.Playlist;
import com.example.music_project.models.Song;
import com.example.music_project.models.User;
import com.example.music_project.views.activities.SongActivity;
import com.example.music_project.views.adapters.AlbumAdapter;
import com.example.music_project.views.adapters.AlbumWithDetailsAdapter;
import com.example.music_project.views.adapters.ArtistAdapter;
import com.example.music_project.views.adapters.ArtistAdapter;
import com.example.music_project.views.adapters.PlaylistAdapter;
import com.example.music_project.views.adapters.SongAdapter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class LibraryFragment extends Fragment {
    private RecyclerView rvLibraryItems;
    private PlaylistController playlistController;
    private AlbumController albumController;
    private UserController userController;
    private GenreController genreController;
    private ArtistController artistController;

    private Button btnPlaylist, btnAlbum, btnArtist;

    private ImageView iv_search_library;
    private PlaylistAdapter playlistAdapter;
    private AlbumAdapter albumAdapter;
    private SongAdapter songAdapter;
    private ArtistAdapter artistAdapter;

    // Danh sách playlist và album
    private List<Playlist> playlistList = new ArrayList<>();
    private List<Album> albumList = new ArrayList<>();
    private List<Artist> artistList = new ArrayList<>();
    private Handler mainHandler;
    private Uri selectedImageUri = null;
    private static final int PICK_IMAGE_REQUEST = 1; // Define the request code

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_library, container, false);

        // Khởi tạo controller và RecyclerView
        playlistController = new PlaylistController(getContext());
        albumController = new AlbumController(getContext());
        userController = new UserController(getContext());
        iv_search_library = view.findViewById(R.id.iv_search_library);
        artistController = new ArtistController(getContext());
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

        iv_search_library.setOnClickListener(v -> {
            // Chuyển sang SearchFragment
            SearchFragment searchFragment = new SearchFragment();
            getActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, searchFragment)
                    .addToBackStack(null)
                    .commit();
        });

        // Thiết lập sự kiện cho các nút
        setupListeners();

        // Lấy User ID hiện tại và lưu vào SharedPreferences
        getCurrentUserId();
        loadPlaylists();

        return view;
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
            loadArtists();
            rvLibraryItems.setAdapter(artistAdapter);
        });
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
                showAddArtistDialog();
                return true;
            }
            if (item.getItemId() == R.id.add_song) {
                openSongScreen();
                return true;
            }
            return false;
        });

        popupMenu.show();
    }
    private void openSongScreen() {
        userController.getCurrentUser(new UserController.OnUserFetchedListener() {
            @Override
            public void onSuccess(User user) {
                Intent intent = new Intent(getActivity(), SongActivity.class);
                intent.putExtra("USER_ID", user.getUser_id());
                startActivity(intent);
            }

            @Override
            public void onFailure(String error) {
                mainHandler.post(() -> {
                    if (isAdded()) {
                        Toast.makeText(requireContext(), getString(R.string.failed_load_profile, error), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
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

    // Tải danh sách nghệ sĩ
    private void loadArtists() {
        artistController.getArtists(new ArtistController.OnArtistsLoadedListener() {
            @Override
            public void onArtistLoaded(List<Artist> artists) {
                if (artists == null || artists.isEmpty()) {
                    Log.d("DEBUG", "Không có nghệ sĩ nào");
                    return;
                }

                // Cập nhật danh sách nghệ sĩ
                artistList.clear();
                artistList.addAll(artists);

                // Cập nhật adapter
                if (artistAdapter == null) {
                    artistAdapter = new ArtistAdapter(artistList,
                            artist -> loadArtistDetail(artist.getArtist_id(), artist.getArtist_name(),
                                    artist.getBio(), artist.getDate_of_birth()) // Load chi tiết nghệ sĩ
                    );
                    rvLibraryItems.setAdapter(artistAdapter);
                } else {
                    artistAdapter.notifyDataSetChanged(); // Cập nhật dữ liệu
                }
            }

            @Override
            public void onFailure(String error) {
                new Handler(Looper.getMainLooper()).post(() -> Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show());
            }
        });
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

                // Danh sách mới để chứa AlbumWithDetails
                List<AlbumWithDetails> albumDetailsList = new ArrayList<>();

                for (Album album : albums) {
                    int artistId = album.getArtist_id();
                    int genreId = album.getGenre_id();

                    // Tạo đối tượng AlbumWithDetails và thêm vào danh sách
                    artistController.getArtistById(artistId, new ArtistController.OnArtistLoadedListener() {
                        @Override
                        public void onArtistLoaded(Artist artist) {
                            genreController.getGenreById(genreId, new GenreController.OnGenreLoadedListener() {
                                @Override
                                public void onGenreLoaded(Genre genre) {
                                    // Tạo đối tượng AlbumWithDetails với tên tác giả và thể loại
                                    AlbumWithDetails details = new AlbumWithDetails(album, artist.getArtist_name(), genre.getGenre_name());
                                    albumDetailsList.add(details);

                                    // Cập nhật adapter sau khi đã có tất cả dữ liệu
                                    if (albumDetailsList.size() == albums.size()) {
                                        updateAdapter(albumDetailsList);
                                    }
                                }

                                @Override
                                public void onFailure(String error) {
                                    Log.d("DEBUG", error);
                                }
                            });
                        }

                        @Override
                        public void onFailure(String error) {
                            Log.d("DEBUG", error);
                        }
                    });
                }
            }

            @Override
            public void onFailure(String error) {
                new Handler(Looper.getMainLooper()).post(() -> Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show());
            }
        });
    }

    // Cập nhật adapter với danh sách AlbumWithDetails
    private void updateAdapter(List<AlbumWithDetails> albumDetailsList) {
        // Chạy trên luồng chính để cập nhật adapter
        new Handler(Looper.getMainLooper()).post(() -> {
            // Cập nhật adapter với albumDetailsList
            AlbumWithDetailsAdapter albumWithDetailsAdapter = new AlbumWithDetailsAdapter(albumDetailsList,
                    albumWithDetails -> loadAlbumDetail(albumWithDetails.getAlbum().getAlbum_id(),
                            albumWithDetails.getAlbum().getTitle(),
                            albumWithDetails.getAlbum().getArtist_id(),
                            albumWithDetails.getAlbum().getGenre_id()),
                    albumWithDetails -> showEditAlbumDialog(albumWithDetails.getAlbum()));  // Handle long press event

            rvLibraryItems.setAdapter(albumWithDetailsAdapter);
        });
    }

    // Phương thức tải bài hát trong playlist và chuyển sang màn hình chi tiết
    private void loadSongsInPlaylist(int playlistId, String playlistName, String userName) {
        DetailPlaylistFragment detailFragment = DetailPlaylistFragment.newInstance(playlistId, playlistName, userName);
        getActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, detailFragment)
                .addToBackStack(null)
                .commit();
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
                               // Toast.makeText(getContext(), "Danh sách phát " + playlistName + " được tạo", Toast.LENGTH_SHORT).show();
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

    @Override
    public void onResume() {
        super.onResume();
        loadPlaylists();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getParentFragmentManager().setFragmentResultListener("requestKey", this, (requestKey, result) -> {
            if (result.getBoolean("playlistDeleted", false)) {
                loadPlaylists();
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
        EditText edtReleaseDate = dialogView.findViewById(R.id.edt_release_date);
        ImageView imgAlbumCover = dialogView.findViewById(R.id.img_album_cover);
        // Set default album cover
        Glide.with(this)
                .load(R.drawable.sample_album_cover)
                .error(R.drawable.default_album_art)
                .into(imgAlbumCover);

        // Pre-fill the release date EditText with the current date in dd-MM-yyyy format
        String currentDate = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date());
        edtReleaseDate.setText(currentDate);

        // Set onClickListener for album cover selection
        imgAlbumCover.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, PICK_IMAGE_REQUEST);
        });

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
                    String releaseDateStr = edtReleaseDate.getText().toString().trim(); // Get the user-entered or pre-filled date

                    if (!albumName.isEmpty() && selectedGenre != null) { // Kiểm tra selectedGenre không null

                        int genreId = selectedGenre.getGenre_id(); // Lấy genreId từ đối tượng Genre

                        // Lấy userId từ SharedPreferences làm artistId
                        SharedPreferences preferences = getActivity().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
                        long userIdLong = preferences.getLong("userId", -1);
                        int userId = (int) userIdLong;

                        Date releaseDate;
                        try {
                            releaseDate = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).parse(releaseDateStr);
                        } catch (ParseException e) {
                            releaseDate = new Date();
                        }

                        Album album = new Album(albumName, userId, genreId, releaseDate);

                        // Nếu selectedImageUri không phải là null, thì lưu ảnh
                        if (selectedImageUri != null) {
                            String albumCoverPath = saveImageToStorage(selectedImageUri);
                            album.setCover_image_path(albumCoverPath);

                            // Load selected image into ImageView using Glide
                            Glide.with(this)
                                    .load(selectedImageUri)
                                    .error(R.drawable.default_album_art)
                                    .into(imgAlbumCover);
                        }

                        // Call AlbumController to create the album
                        albumController.createAlbum(userId, album, new AlbumController.OnAlbumCreatedListener() {
                            @Override
                            public void onSuccess() {
                                new Handler(Looper.getMainLooper()).post(() -> {
                                    Toast.makeText(getContext(), "Album " + albumName + " được tạo", Toast.LENGTH_SHORT).show();
                                });
                                loadAlbums();
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

    private String saveImageToStorage(Uri imageUri) {
        // Implement logic to save the image and return the file path
        return imageUri.getPath(); // Just a placeholder, handle saving the image properly
    }

    private void loadAlbumDetail(int albumId, String albumName, int artistName, int genreName) {
        AlbumFragment albumDetailFragment = AlbumFragment.newInstance(albumId, albumName, artistName, genreName);
        getActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, albumDetailFragment)
                .addToBackStack(null)
                .commit();
    }

    private void showAddArtistDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_add_artist, null);
        builder.setView(dialogView);

        EditText edtArtistName = dialogView.findViewById(R.id.edt_artist_name);
        EditText edtArtistDob = dialogView.findViewById(R.id.edt_artist_dob); // Cho phép nhập trực tiếp vào đây
        EditText edtArtistBio = dialogView.findViewById(R.id.edt_artist_bio);
        ImageButton btnSelectDate = dialogView.findViewById(R.id.btn_select_date); // Nút để chọn lịch

        // Tạo biến Date để lưu ngày sinh (nếu cần xử lý sau khi chọn)
        final Date[] artistDob = {null};

        // Cho phép nhập trực tiếp vào edtArtistDob

        // Hiển thị DatePickerDialog khi nhấn vào nút btnSelectDate
        btnSelectDate.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            // Hiển thị DatePickerDialog
            DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(),
                    (view, selectedYear, selectedMonth, selectedDay) -> {
                        // Cập nhật ngày đã chọn vào EditText
                        String selectedDate = String.format(Locale.getDefault(), "%02d-%02d-%d",
                                selectedDay, selectedMonth + 1, selectedYear);
                        edtArtistDob.setText(selectedDate);

                        // Cập nhật biến artistDob nếu cần sử dụng sau
                        Calendar selectedCalendar = Calendar.getInstance();
                        selectedCalendar.set(selectedYear, selectedMonth, selectedDay);
                        artistDob[0] = selectedCalendar.getTime();
                    },
                    year, month, day);

            datePickerDialog.show(); // Hiển thị lịch chọn ngày
        });

        // Xử lý logic tạo nghệ sĩ mới khi nhấn nút tạo
        builder.setTitle("Thêm Nghệ Sĩ Mới")
                .setPositiveButton("Tạo", (dialog, id) -> {
                    String artistName = edtArtistName.getText().toString().trim();
                    String artistBio = edtArtistBio.getText().toString().trim();
                    String artistDobString = edtArtistDob.getText().toString().trim();

                    // Xác nhận rằng tất cả các trường đều không trống
                    if (!artistName.isEmpty() && !artistBio.isEmpty() && !artistDobString.isEmpty()) {

                        // Chuyển đổi artistDobString thành Date (nếu cần)
                        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
                        try {
                            artistDob[0] = dateFormat.parse(artistDobString);
                        } catch (ParseException e) {
                            e.printStackTrace();
                            Toast.makeText(getContext(), "Định dạng ngày không hợp lệ", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        // Tạo nghệ sĩ mới
                        Artist artist = new Artist(artistName, artistBio, artistDob[0]);

                        // Gọi hàm tạo nghệ sĩ từ ArtistController
                        artistController.createArtist(artist, new ArtistController.OnArtistCreatedListener() {
                            @Override
                            public void onSuccess() {
                                new Handler(Looper.getMainLooper()).post(() -> {
                                    Toast.makeText(getContext(), "Nghệ sĩ " + artistName + " được tạo", Toast.LENGTH_SHORT).show();
                                });
                                loadArtists(); // Tải lại danh sách nghệ sĩ
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
                            Toast.makeText(getContext(), "Tên nghệ sĩ, bio và ngày sinh không được để trống", Toast.LENGTH_SHORT).show();
                        });
                    }
                })
                .setNegativeButton("Hủy", (dialog, id) -> dialog.dismiss())
                .create()
                .show();
    }

    private void loadArtistDetail(int artistId, String artistName, String bio, Date dateOfBirth) {
        // Khởi tạo Fragment chi tiết nghệ sĩ
        ArtistDetailFragment artistDetailFragment = ArtistDetailFragment.newInstance(artistId, artistName, bio, dateOfBirth);

        // Thay thế Fragment hiện tại bằng Fragment chi tiết nghệ sĩ
        getActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, artistDetailFragment)  // Thay R.id.fragment_container bằng id của container Fragment trong layout của bạn
                .addToBackStack(null)  // Thêm vào back stack để có thể quay lại
                .commit();
    }

    // Function to show the Album edit dialog
    private void showEditAlbumDialog(Album album) {
        DialogEditAlbumFragment dialogEditAlbumFragment = DialogEditAlbumFragment.newInstance(album.getAlbum_id());
        dialogEditAlbumFragment.setOnAlbumEditedListener(() -> {
            Toast.makeText(getContext(), "Album đã được chỉnh sửa", Toast.LENGTH_SHORT).show();
        });
        dialogEditAlbumFragment.show(getFragmentManager(), "edit_album");
    }
}
