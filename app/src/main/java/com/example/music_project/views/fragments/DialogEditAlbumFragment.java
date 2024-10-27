package com.example.music_project.views.fragments;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.music_project.R;
import com.example.music_project.controllers.AlbumController;
import com.example.music_project.models.Album;
import com.example.music_project.models.Song;
import com.example.music_project.views.adapters.SongAdapter;

import java.util.ArrayList;
import java.util.List;

public class DialogEditAlbumFragment extends DialogFragment {

    private ImageView imgAlbumCover;
    private EditText etAlbumTitle;
    private RecyclerView rvSongs;
    private SongAdapter songAdapter;
    private List<Song> songList = new ArrayList<>();
    private AlbumController albumController;
    private OnAlbumEditedListener onAlbumEditedListener;

    private int albumId; // Lưu album ID để dễ quản lý
    private String currentCoverImagePath; // Lưu đường dẫn ảnh hiện tại
    private Uri newImageUri;

    public static DialogEditAlbumFragment newInstance(int albumId) {
        DialogEditAlbumFragment fragment = new DialogEditAlbumFragment();
        Bundle args = new Bundle();
        args.putInt("album_id", albumId);
        fragment.setArguments(args);
        return fragment;
    }

    private final ActivityResultLauncher<Intent> selectImageLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == getActivity().RESULT_OK && result.getData() != null) {
                    newImageUri = result.getData().getData();
                    if (newImageUri != null) {
                        Glide.with(this)
                                .load(newImageUri)
                                .into(imgAlbumCover);
                    }
                }
            }
    );

    @Override
    public void onStart() {
        super.onStart();
        // Set custom dimensions for the dialog
        if (getDialog() != null) {
            int width = ViewGroup.LayoutParams.MATCH_PARENT;
            int height = ViewGroup.LayoutParams.WRAP_CONTENT;
            getDialog().getWindow().setLayout(width, height);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_edit_album, container, false);

        imgAlbumCover = view.findViewById(R.id.img_album_cover);
        etAlbumTitle = view.findViewById(R.id.et_album_title);
        rvSongs = view.findViewById(R.id.rv_album_songs);
        Button btnSave = view.findViewById(R.id.btn_album_update); // Nút lưu
        Button btnCancel = view.findViewById(R.id.btn_album_cancle); // Nút hủy
        Button btnDelete = view.findViewById(R.id.btn_album_delete);

        albumController = new AlbumController(getContext());

        if (getArguments() != null) {
            albumId = getArguments().getInt("album_id");
        }

        rvSongs.setLayoutManager(new LinearLayoutManager(getContext()));

        loadAlbumDetails(albumId);

        songAdapter = new SongAdapter(songList, song -> Toast.makeText(getContext(), "Selected: " + song.getTitle(), Toast.LENGTH_SHORT).show());

//        songAdapter.setOnSongLongClickListener(new SongAdapter.OnSongLongClickListener() {
//            @Override
//            public void onSongLongClick(Song song) {
//                // Hiển thị hộp thoại xác nhận khi nhấn lâu
//                showDeleteSongConfirmationDialog(song);
//            }
//        });

        rvSongs.setAdapter(songAdapter);

        imgAlbumCover.setOnClickListener(v -> selectImage());

        // Xử lý sự kiện nút lưu
        btnSave.setOnClickListener(v -> {
            saveAlbumDetails();
        });

        // Xử lý sự kiện nút hủy
        btnCancel.setOnClickListener(v -> dismiss());

        // Xử lý sự kiện nút xóa
        btnDelete.setOnClickListener(v -> deleteAlbum());

        return view;
    }

    private void selectImage() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        selectImageLauncher.launch(intent);
    }

    private void loadAlbumDetails(int albumId) {
        albumController.getAlbumById(albumId, new AlbumController.OnAlbumLoadedListener() {
            @Override
            public void onAlbumLoaded(Album album) {
                if (album != null) {
                    // Hiển thị ảnh cover và album title
                    etAlbumTitle.setText(album.getTitle());
                    currentCoverImagePath = album.getCover_image_path();
                    if (currentCoverImagePath != null && !currentCoverImagePath.isEmpty()) {
                        Glide.with(DialogEditAlbumFragment.this)
                                .load(currentCoverImagePath)
                                .into(imgAlbumCover);
                    }
                    // Tải danh sách bài hát
                    loadSongsInAlbum(albumId);
                }
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(getContext(), "Failed to load album: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadSongsInAlbum(int albumId) {
        albumController.getSongsInAlbum(albumId, new AlbumController.OnSongsLoadedListener() {
            @Override
            public void onSongsLoaded(List<Song> songs) {
                if (songs != null && !songs.isEmpty()) {
                    songList.clear();
                    songList.addAll(songs);
                    songAdapter.notifyDataSetChanged(); // Cập nhật adapter
                } else {
                    Toast.makeText(getContext(), "Không có bài hát nào trong album", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(String error) {
                // Switch to the main thread to show the Toast
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(), "Failed to load album: " + error, Toast.LENGTH_SHORT).show();
                    });
                }
            }
        });
    }

    private void saveAlbumDetails() {
        String newTitle = etAlbumTitle.getText().toString();
        String newCoverImagePath = newImageUri != null ? newImageUri.toString() : currentCoverImagePath; // Nếu bạn cho phép thay đổi ảnh cover, cập nhật ở đây

        // Cập nhật album trong cơ sở dữ liệu
        albumController.updateAlbum(albumId, newTitle, newCoverImagePath, new AlbumController.OnAlbumUpdatedListener() {
            @Override
            public void onAlbumUpdated() {
                Toast.makeText(getContext(), "Album chỉnh sửa thành công!", Toast.LENGTH_SHORT).show();
                if (onAlbumEditedListener != null) {
                    onAlbumEditedListener.onAlbumEdited();
                }
                dismiss();
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(getContext(), "Album chỉnh sửa thất bại: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deleteAlbum() {
        // Hiển thị dialog xác nhận xóa
        new AlertDialog.Builder(getContext())
                .setTitle("Xóa Album")
                .setMessage("Bạn có chắc chắn muốn xóa album này không?")
                .setPositiveButton("Có", (dialog, which) -> {
                    albumController.deleteAlbum(albumId, new AlbumController.OnAlbumDeletedListener() {
                        @Override
                        public void onAlbumDeleted() {
                            Toast.makeText(getContext(), "Album được xóa thành công!", Toast.LENGTH_SHORT).show();
                            if (onAlbumEditedListener != null) {
                                onAlbumEditedListener.onAlbumEdited();
                            }
                            // Navigate back to the library fragment
                            if (getActivity() != null) {
                                getActivity().getSupportFragmentManager().popBackStack(); // or use an Intent to go back to the library activity
                            }
                            dismiss();
                        }

                        @Override
                        public void onFailure(String error) {
                            Toast.makeText(getContext(), "Album xóa thất bại: " + error, Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton("Không", null)
                .show();
    }
//
//    private void showDeleteSongConfirmationDialog(Song song) {
//        new AlertDialog.Builder(requireContext())
//                .setTitle("Xóa Bài Hát")
//                .setMessage("Bạn có chắc chắn muốn xóa bài hát \"" + song.getTitle() + "\" khỏi album không?")
//                .setPositiveButton("Có", (dialog, which) -> {
//                    deleteSongFromAlbum(song);
//                    loadSongsInAlbum(albumId);
//                })
//                .setNegativeButton("Không", null)
//                .show();
//    }
//
//    private void deleteSongFromAlbum(Song song) {
//        int albumId = getArguments().getInt("album_id");
//        albumController.deleteSongFromAlbum(albumId, song.getSong_id(), new AlbumController.OnSongDeletedListener() {
//            @Override
//            public void onSongDeleted(Song song) {
//                Toast.makeText(getContext(), "Đã xóa bài hát khỏi playlist", Toast.LENGTH_SHORT).show();
//                loadSongsInAlbum(albumId);
//            }
//
//            @Override
//            public void onFailure(String error) {
//                Toast.makeText(getContext(), "Lỗi khi xóa bài hát: " + error, Toast.LENGTH_SHORT).show();
//            }
//        });
//    }

    public void setOnAlbumEditedListener(OnAlbumEditedListener listener) {
        this.onAlbumEditedListener = listener;
    }

    public interface OnAlbumEditedListener {
        void onAlbumEdited();
    }
}
