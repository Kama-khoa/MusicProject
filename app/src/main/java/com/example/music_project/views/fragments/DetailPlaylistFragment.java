package com.example.music_project.views.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.music_project.R;
import com.example.music_project.controllers.PlaylistController;
import com.example.music_project.models.Playlist;
import com.example.music_project.models.Song;
import com.example.music_project.views.activities.SongActivity;
//import com.example.music_project.views.activities.SongSelectionActivity;
import com.example.music_project.views.activities.SongSelectionActivity;
import com.example.music_project.views.adapters.SongAdapter;

import java.util.ArrayList;
import java.util.List;

public class DetailPlaylistFragment extends Fragment {
    private static final String ARG_PLAYLIST_ID = "playlist_id";
    private static final String ARG_PLAYLIST_NAME = "playlist_name";
    private static final String ARG_USER_NAME = "user_name";
    private RecyclerView rvSongs;
    private SongAdapter songAdapter;
    private List<Song> songList = new ArrayList<>();
    private List<Song> fullSongList = new ArrayList<>();
    private Uri newImageUri;
    private ImageView imgPlaylistCover;

    private TextView tvPlaylistName;
    private TextView tvPlaylistUserName;
    private TextView tvPlaylistDescription;
    private int playlistId;
    private Button btn_add_song_to_playlist;

    private ActivityResultLauncher<Intent> songSelectionLauncher ;
    private ActivityResultLauncher<Intent> selectImageLauncher;


//    private OnPlaylistDeletedListener listener;
//    public interface OnPlaylistDeletedListener {
//        void onPlaylistDeleted();
//    }
//
public static DetailPlaylistFragment newInstance(int playlistId, String playlistName, String userName) {
    DetailPlaylistFragment fragment = new DetailPlaylistFragment();
    Bundle args = new Bundle();
    args.putInt("playlist_id", playlistId);
    args.putString("playlist_name", playlistName);
    args.putString("user_name", userName); // Lưu tên người dùng vào Bundle
    fragment.setArguments(args);
    return fragment;
}



//    @Override
//    public void onAttach(@NonNull Context context) {
//        super.onAttach(context);
//        if (context instanceof OnPlaylistDeletedListener) {
//            listener = (OnPlaylistDeletedListener) context;
//        } else {
//            throw new RuntimeException(context.toString() + " must implement OnPlaylistDeletedListener");
//        }
//    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_playlist_detail, container, false);
        rvSongs = view.findViewById(R.id.rv_songs);
        rvSongs.setLayoutManager(new LinearLayoutManager(getContext()));
        songAdapter = new SongAdapter(songList, new SongAdapter.OnSongClickListener() {
            @Override
            public void onSongClick(Song song) {
                // Xử lý khi nhấn ngắn vào bài hát
                Toast.makeText(getContext(), "Đã chọn: " + song.getTitle(), Toast.LENGTH_SHORT).show();
            }
        });

// Sau khi khởi tạo songAdapter, thiết lập OnSongLongClickListener thông qua setter
        songAdapter.setOnSongLongClickListener(new SongAdapter.OnSongLongClickListener() {
            @Override
            public void onSongLongClick(Song song) {
                // Hiển thị hộp thoại xác nhận khi nhấn lâu
                showDeleteSongConfirmationDialog(song);
            }
        });

        rvSongs.setAdapter(songAdapter);

        tvPlaylistName = view.findViewById(R.id.tv_playlist_name);
        tvPlaylistUserName = view.findViewById(R.id.tv_playlist_user_name);
        tvPlaylistDescription = view.findViewById(R.id.tv_playlist_details);
        playlistId = getArguments().getInt(ARG_PLAYLIST_ID);
        btn_add_song_to_playlist = view.findViewById(R.id.btn_add_song);
        imgPlaylistCover = view.findViewById(R.id.img_playlist_cover);

        songSelectionLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Song selectedSong = (Song) result.getData().getSerializableExtra("selected_song");
                        if (selectedSong != null) {
                            addSongToPlaylist(selectedSong);
                            // Tải lại danh sách bài hát trong playlist sau khi thêm bài mới
                            loadSongsInPlaylist(playlistId, getArguments().getString(ARG_PLAYLIST_NAME), String.valueOf(getArguments().getInt("user_id")));
                        }
                    }
                }
        );

        selectImageLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        newImageUri = result.getData().getData();
                        if (newImageUri != null) {
                            Glide.with(this)
                                    .load(newImageUri)
                                    .into(imgPlaylistCover);
                        }
                    }
                }
        );


        // Tìm kiếm
        SearchView searchView = view.findViewById(R.id.sv_search);
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

        // Tải danh sách bài hát
        if (getArguments() != null) {
            String playlistName = getArguments().getString(ARG_PLAYLIST_NAME);
            String userName = getArguments().getString(ARG_USER_NAME);

            loadPlaylistDetails(playlistId, playlistName, userName);

            loadSongsInPlaylist(playlistId, playlistName, userName);
        }

//        if (newImageUri != null) {
//            Glide.with(this)
//                    .load(newImageUri)
//                    .into(imgPlaylistCover);
//        }

        btn_add_song_to_playlist.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), SongSelectionActivity.class);
            intent.putExtra("PLAYLIST_ID", playlistId);
            songSelectionLauncher.launch(intent); // Chỉ cần gọi launcher
        });

        ImageView imgEditOrDel = view.findViewById(R.id.img_playlist_edit_or_del);

        imgEditOrDel.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(getContext(), v);
            popup.getMenuInflater().inflate(R.menu.menu_playlist_edit_or_del, popup.getMenu());

            popup.setOnMenuItemClickListener(item -> {
                int itemId = item.getItemId();

                if (itemId == R.id.edit_playlist) {
                    showEditPlaylistDialog();
                    return true;
                } else if (itemId == R.id.delete_playlist) {
                    showDeleteConfirmationDialog();
                    return true;
                } else {
                    return false;
                }
            });

            popup.show();
        });

        loadSongsInPlaylist(playlistId, getArguments().getString(ARG_PLAYLIST_NAME), getArguments().getString(ARG_USER_NAME));

        return view;
    }


    private void loadPlaylistDetails(int playlistId, String playlistName, String userName) {
        PlaylistController playlistController = new PlaylistController(getContext());

        // Lấy chi tiết playlist bao gồm ảnh bìa
        playlistController.getPlaylistDetails(playlistId, new PlaylistController.OnPlaylistDetailsLoadedListener() {
            @Override
            public void onPlaylistDetailsLoaded(Playlist playlist) {
                if (playlist != null) {
                    // Hiển thị tên playlist và tên người dùng
                    tvPlaylistName.setText(playlist.getTitle());
                    tvPlaylistUserName.setText(userName);
                    tvPlaylistDescription.setText(playlist.getDetails());

                    // Lấy đường dẫn ảnh từ playlist
                    String imagePath = playlist.getImageResource();

                    if (imagePath != null && !imagePath.isEmpty()) {
                        // Sử dụng Glide để hiển thị ảnh bìa playlist
                        Glide.with(getContext())
                                .load(imagePath)
                                .into(imgPlaylistCover);
                    } else {
                        // Nếu không có ảnh, bạn có thể đặt ảnh mặc định
                        imgPlaylistCover.setImageResource(R.drawable.ic_image_playlist); // Thay bằng ảnh mặc định của bạn
                    }
                }
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(getContext(), "Lỗi khi tải chi tiết playlist: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void loadSongsInPlaylist(int playlistId, String playlistName, String userName) {
        PlaylistController playlistController = new PlaylistController(getContext());
        playlistController.getSongsInPlaylist(playlistId, new PlaylistController.OnSongsLoadedListener() {
            @Override
            public void onSongsLoaded(List<Song> songs) {
                if (songs != null && !songs.isEmpty()) {
                    songList.clear();
                    songList.addAll(songs);
                    fullSongList.clear();
                    fullSongList.addAll(songs); // Lưu danh sách đầy đủ để tìm kiếm
                    songAdapter.notifyDataSetChanged();

//                    // Nếu playlist có ảnh
//                    String imagePath = playlist.getImageResource(); // Lấy đường dẫn ảnh từ playlist
//                    if (imagePath != null) {
//                        Glide.with(getContext()).load(imagePath).into(imgPlaylistCover);
//                    }
                } else {
                    Toast.makeText(getContext(), "Không có bài hát nào trong playlist", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
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

    private void showEditPlaylistDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_edit_playlist, null);
        builder.setView(dialogView);

        EditText edtPlaylistName = dialogView.findViewById(R.id.edt_playlist_name);
        EditText edtPlaylistDescription = dialogView.findViewById(R.id.edt_playlist_description);
        ImageView imgPlaylistCoverDialog = dialogView.findViewById(R.id.img_playlist_cover);

        // Cập nhật ảnh bìa nếu có
        if (newImageUri != null) {
            Glide.with(this)
                    .load(newImageUri)
                    .into(imgPlaylistCoverDialog); // Tải ảnh vào dialog
        }

        imgPlaylistCoverDialog.setOnClickListener(v -> {
            selectImage(imgPlaylistCoverDialog); // Truyền imgPlaylistCoverDialog vào phương thức selectImage
        });

        builder.setPositiveButton("Cập nhật", (dialog, which) -> {
            String newPlaylistName = edtPlaylistName.getText().toString();
            String newPlaylistDescription = edtPlaylistDescription.getText().toString();
            if (newPlaylistName.isEmpty()) {
                Toast.makeText(getContext(), "Tên playlist không được để trống!", Toast.LENGTH_SHORT).show();
                return; // Dừng lại nếu tên trống
            }
            updatePlaylistInDatabase(newPlaylistName, newPlaylistDescription, newPlaylistDescription, newImageUri);
        });

        builder.setNegativeButton("Hủy", null);
        builder.create().show();
    }

    private void selectImage(ImageView imgPlaylistCoverDialog) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        selectImageLauncher.launch(intent);
    }

    private void updatePlaylistInDatabase(String newPlaylistName, String newPlaylistDescription,String Description, Uri newImageUri) {
        // Kiểm tra URL của ảnh bìa trước khi cập nhật
        String imagePath = null;
        if (newImageUri != null) {
            imagePath = newImageUri.toString(); // Lưu URI của ảnh vào biến imagePath
            Log.d("PlaylistDebug", "Updating playlist with new Image URI: " + newImageUri.toString());
        } else {
            Log.d("PlaylistDebug", "No Image URI provided for update.");
        }

        PlaylistController playlistController = new PlaylistController(getContext());
        playlistController.updatePlaylist(playlistId, newPlaylistName, newPlaylistDescription, imagePath, new PlaylistController.OnPlaylistUpdatedListener() {
            @Override
            public void onSuccess() {
                // Hiển thị thông báo cập nhật thành công
                Toast.makeText(getContext(), "Cập nhật thành công", Toast.LENGTH_SHORT).show();

                // Cập nhật giao diện với thông tin mới
                tvPlaylistName.setText(newPlaylistName);  // Cập nhật tên playlist trên giao diện
                tvPlaylistUserName.setText(tvPlaylistUserName.getText().toString());
                tvPlaylistDescription.setText(Description);// Giữ nguyên tên người dùng
                if (newImageUri != null) {
                    Glide.with(DetailPlaylistFragment.this)
                            .load(newImageUri)
                            .into(imgPlaylistCover); // Cập nhật ảnh mới lên imgPlaylistCover
                }
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(getContext(), "Lỗi: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void openSongSelection() {
        Intent intent = new Intent(getContext(), SongActivity.class);
        songSelectionLauncher.launch(intent);
    }

    private void addSongToPlaylist(Song song) {
        PlaylistController playlistController = new PlaylistController(getContext());
        playlistController.addSongToPlaylist(playlistId, song, new PlaylistController.OnSongAddedListener() {
            @Override
            public void onSuccess() {
                Toast.makeText(getContext(), "Đã thêm bài hát vào playlist", Toast.LENGTH_SHORT).show();
                // Tải lại danh sách bài hát
                loadSongsInPlaylist(playlistId, getArguments().getString(ARG_PLAYLIST_NAME), getArguments().getString(ARG_USER_NAME));
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(getContext(), "Lỗi: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void showDeleteConfirmationDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Xóa Playlist")
                .setMessage("Bạn có chắc chắn muốn xóa playlist này?")
                .setPositiveButton("Có", (dialog, which) -> deletePlaylist())
                .setNegativeButton("Không", null)
                .show();
    }

    private void deletePlaylist() {
        PlaylistController playlistController = new PlaylistController(getContext());
        playlistController.deletePlaylist(playlistId, new PlaylistController.OnPlaylistDeletedListener() {
            @Override
            public void onSuccess() {
                Bundle result = new Bundle();
                result.putBoolean("playlistDeleted", true);
                getParentFragmentManager().setFragmentResult("requestKey", result);

                // Quay lại LibraryFragment
                getActivity().getSupportFragmentManager().popBackStack();
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(getContext(), "Xóa playlist thất bại: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }
    @Override
    public void onResume() {
        super.onResume();
        loadSongsInPlaylist(playlistId, getArguments().getString(ARG_PLAYLIST_NAME), getArguments().getString(ARG_USER_NAME));
    }


    private void showDeleteSongConfirmationDialog(Song song) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Xóa Bài Hát")
                .setMessage("Bạn có chắc chắn muốn xóa bài hát \"" + song.getTitle() + "\" khỏi playlist không?")
                .setPositiveButton("Có", (dialog, which) -> {
                    deleteSongFromPlaylist(song);
                })
                .setNegativeButton("Không", null)
                .show();
    }

    private void deleteSongFromPlaylist(Song song) {
        PlaylistController playlistController = new PlaylistController(getContext());
        playlistController.deleteSongFromPlaylist(playlistId, song.getSong_id(), new PlaylistController.OnSongDeletedListener() {
            @Override
            public void onSongDeleted(Song song) {
                Toast.makeText(getContext(), "Đã xóa bài hát khỏi playlist", Toast.LENGTH_SHORT).show();
                loadSongsInPlaylist(playlistId, getArguments().getString(ARG_PLAYLIST_NAME), getArguments().getString(ARG_USER_NAME));
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(getContext(), "Lỗi khi xóa bài hát: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }


}
