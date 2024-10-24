package com.example.music_project.views.fragments;

import android.app.AlertDialog;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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

    public static DialogEditAlbumFragment newInstance(int albumId) {
        DialogEditAlbumFragment fragment = new DialogEditAlbumFragment();
        Bundle args = new Bundle();
        args.putInt("album_id", albumId);
        fragment.setArguments(args);
        return fragment;
    }

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

        rvSongs.setLayoutManager(new LinearLayoutManager(getContext()));
        songAdapter = new SongAdapter(songList, song -> Toast.makeText(getContext(), "Selected: " + song.getTitle(), Toast.LENGTH_SHORT).show());
        rvSongs.setAdapter(songAdapter);

        albumController = new AlbumController(getContext());

        if (getArguments() != null) {
            albumId = getArguments().getInt("album_id");
            loadAlbumDetails(albumId); // Tải thông tin album và bài hát
        }

        // Xử lý sự kiện nút lưu
        btnSave.setOnClickListener(v -> {
            saveAlbumDetails();
            loadAlbumDetails(albumId);
        });

        // Xử lý sự kiện nút hủy
        btnCancel.setOnClickListener(v -> dismiss());

        // Xử lý sự kiện nút xóa
        btnDelete.setOnClickListener(v -> deleteAlbum());

        // Add swipe-to-delete functionality
        // setupSwipeToDelete();

        return view;
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
                        // Hiển thị ảnh cover (bạn cần thêm logic load ảnh từ file/URI)
                        imgAlbumCover.setImageURI(Uri.parse(currentCoverImagePath));
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
                songList.clear();
                songList.addAll(songs);
                songAdapter.notifyDataSetChanged();
            }

            @Override
            public void onFailure(String error) {
                getActivity().runOnUiThread(() ->
                        Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show()
                );
            }
        });
    }

    private void saveAlbumDetails() {
        String newTitle = etAlbumTitle.getText().toString();
        String newCoverImagePath = currentCoverImagePath; // Nếu bạn cho phép thay đổi ảnh cover, cập nhật ở đây

        // Cập nhật album trong cơ sở dữ liệu
        albumController.updateAlbum(albumId, newTitle, newCoverImagePath, new AlbumController.OnAlbumUpdatedListener() {
            @Override
            public void onAlbumUpdated() {
                Toast.makeText(getContext(), "Album updated successfully", Toast.LENGTH_SHORT).show();
                if (onAlbumEditedListener != null) {
                    onAlbumEditedListener.onAlbumEdited();
                }
                loadAlbumDetails(albumId);
                dismiss();
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(getContext(), "Failed to update album: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deleteAlbum() {
        // Hiển thị dialog xác nhận xóa
        new AlertDialog.Builder(getContext())
                .setTitle("Delete Album")
                .setMessage("Are you sure you want to delete this album?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    albumController.deleteAlbum(albumId, new AlbumController.OnAlbumDeletedListener() {
                        @Override
                        public void onAlbumDeleted() {
                            Toast.makeText(getContext(), "Album deleted successfully", Toast.LENGTH_SHORT).show();
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
                            Toast.makeText(getContext(), "Failed to delete album: " + error, Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton("No", null)
                .show();
    }

//    private void setupSwipeToDelete() {
//        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
//            @Override
//            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
//                return false;
//            }
//
//            @Override
//            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
//                // Get the position of the song to delete
//                int position = viewHolder.getAdapterPosition();
//                Song song = songList.get(position);
//                // Remove the song from the list and update adapter
//                songList.remove(position);
//                songAdapter.notifyItemRemoved(position);
//                // Optionally, call the controller to delete the song from the album in the database
//                albumController.deleteSongFromAlbum(song.getId(), new AlbumController.OnSongDeletedListener() {
//                    @Override
//                    public void onSongDeleted() {
//                        Toast.makeText(getContext(), "Deleted: " + song.getTitle(), Toast.LENGTH_SHORT).show();
//                    }
//
//                    @Override
//                    public void onFailure(String error) {
//                        Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
//                    }
//                });
//            }
//        });
//        itemTouchHelper.attachToRecyclerView(rvSongs);
//    }

    public void setOnAlbumEditedListener(OnAlbumEditedListener listener) {
        this.onAlbumEditedListener = listener;
    }

    public interface OnAlbumEditedListener {
        void onAlbumEdited();
    }
}
