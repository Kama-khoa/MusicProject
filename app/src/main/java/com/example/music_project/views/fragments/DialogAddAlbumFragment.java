//package com.example.music_project.views.fragments;
//
//import android.app.AlertDialog;
//import android.app.Dialog;
//import android.content.Intent;
//import android.content.SharedPreferences;
//import android.net.Uri;
//import android.os.Bundle;
//import android.os.Handler;
//import android.os.Looper;
//import android.provider.MediaStore;
//import android.util.Log;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.widget.ArrayAdapter;
//import android.widget.EditText;
//import android.widget.ImageView;
//import android.widget.Spinner;
//import android.widget.Toast;
//
//import androidx.annotation.Nullable;
//import androidx.fragment.app.DialogFragment;
//
//import com.example.music_project.R;
//import com.example.music_project.controllers.AlbumController;
//import com.example.music_project.controllers.GenreController;
//import com.example.music_project.models.Album;
//import com.example.music_project.models.Genre;
//import com.example.music_project.views.adapters.AlbumAdapter;
//
//import java.text.ParseException;
//import java.text.SimpleDateFormat;
//import java.util.Date;
//import java.util.List;
//import java.util.Locale;
//
//public class AlbumAddDialogFragment extends DialogFragment {
//
//    private AlbumController albumController;
//    private GenreController genreController;
//    private Uri selectedImageUri;
//    private List<Album> albumList;
//
//    @Nullable
//    @Override
//    public Dialog onCreateDialog(Bundle savedInstanceState) {
//        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
//        LayoutInflater inflater = requireActivity().getLayoutInflater();
//        View dialogView = inflater.inflate(R.layout.dialog_add_album, null);
//        builder.setView(dialogView);
//
//        EditText edtAlbumName = dialogView.findViewById(R.id.edt_album_name);
//        Spinner spinnerGenre = dialogView.findViewById(R.id.spinner_genre);
//        EditText edtReleaseDate = dialogView.findViewById(R.id.edt_release_date);
//        ImageView imgAlbumCover = dialogView.findViewById(R.id.img_album_cover);
//
//        // Pre-fill the release date EditText with the current date
//        String currentDate = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date());
//        edtReleaseDate.setText(currentDate);
//
//        // Set onClickListener for the album cover ImageView to select an image
//        imgAlbumCover.setOnClickListener(v -> {
//            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
//            startActivityForResult(intent, PICK_IMAGE_REQUEST);
//        });
//
//        // Load genre data from the database
//        genreController.getAllGenres(new GenreController.OnGenresLoadedListener() {
//            @Override
//            public void onGenresLoaded(List<Genre> genres) {
//                getActivity().runOnUiThread(() -> {
//                    ArrayAdapter<Genre> adapter = new ArrayAdapter<>(getContext(),
//                            android.R.layout.simple_spinner_item, genres);
//                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//                    spinnerGenre.setAdapter(adapter);
//                });
//            }
//
//            @Override
//            public void onFailure(String error) {
//                Toast.makeText(getContext(), "Không thể tải danh sách thể loại", Toast.LENGTH_SHORT).show();
//            }
//        });
//
//        builder.setTitle("Thêm Album Mới")
//                .setPositiveButton("Tạo", (dialog, id) -> {
//                    String albumName = edtAlbumName.getText().toString().trim();
//                    Genre selectedGenre = (Genre) spinnerGenre.getSelectedItem();
//                    String releaseDateStr = edtReleaseDate.getText().toString().trim();
//
//                    if (!albumName.isEmpty() && selectedGenre != null && selectedImageUri != null && !releaseDateStr.isEmpty()) {
//                        int genreId = selectedGenre.getGenre_id();
//
//                        // Get userId from SharedPreferences for artistId
//                        SharedPreferences preferences = getActivity().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
//                        long userIdLong = preferences.getLong("userId", -1);
//                        int userId = (int) userIdLong;
//
//                        Date releaseDate;
//                        try {
//                            releaseDate = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).parse(releaseDateStr);
//                        } catch (ParseException e) {
//                            releaseDate = new Date();
//                        }
//
//                        Album album = new Album(albumName, userId, genreId, releaseDate, null);
//
//                        String albumCoverPath = saveImageToStorage(selectedImageUri);
//                        album.setCover_image_path(albumCoverPath);
//
//                        albumController.createAlbum(userId, album, new AlbumController.OnAlbumCreatedListener() {
//                            @Override
//                            public void onSuccess() {
//                                Toast.makeText(getContext(), "Album " + albumName + " được tạo", Toast.LENGTH_SHORT).show();
//                                loadAlbums();
//                            }
//
//                            @Override
//                            public void onFailure(String error) {
//                                Toast.makeText(getContext(), "Lỗi: " + error, Toast.LENGTH_SHORT).show();
//                            }
//                        });
//                    } else {
//                        Toast.makeText(getContext(), "Tên album, thể loại, ngày phát hành và ảnh không được để trống", Toast.LENGTH_SHORT).show();
//                    }
//                })
//                .setNegativeButton("Hủy", (dialog, id) -> dialog.dismiss())
//                .create();
//
//        return builder.create();
//    }
//
//    private String saveImageToStorage(Uri imageUri) {
//        // Implement logic to save the image and return the file path
//        return imageUri.getPath(); // Placeholder
//    }
//
//    // Tải danh sách album
//    private void loadAlbums() {
//        albumController.getAlbums(new AlbumController.OnAlbumsLoadedListener() {
//            @Override
//            public void onAlbumsLoaded(List<Album> albums) {
//                if (albums == null || albums.isEmpty()) {
//                    Log.d("DEBUG", "Không có album nào");
//                    return;
//                }
//
//                // Cập nhật danh sách album
//                albumList.clear();
//                albumList.addAll(albums);
//
//                // Cập nhật adapter
//                if (albumAdapter == null) {
//                    albumAdapter = new AlbumAdapter(albumList, album -> {
//                        loadAlbumDetail( album.getAlbum_id(), album.getTitle(), String.valueOf(album.getArtist_id()), String.valueOf(album.getGenre_id()));
//                    });
//                    rvLibraryItems.setAdapter(albumAdapter);
//                } else {
//                    albumAdapter.notifyDataSetChanged(); // Cập nhật dữ liệu
//                }
//            }
//
//            @Override
//            public void onFailure(String error) {
//                new Handler(Looper.getMainLooper()).post(() -> Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show());
//            }
//        });
//    }
//}
//
