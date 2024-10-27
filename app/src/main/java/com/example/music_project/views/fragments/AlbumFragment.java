package com.example.music_project.views.fragments;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.music_project.R;
import com.example.music_project.controllers.AlbumController;
import com.example.music_project.controllers.ArtistController;
import com.example.music_project.controllers.GenreController;
import com.example.music_project.models.Album;
import com.example.music_project.models.Artist;
import com.example.music_project.models.Genre;
import com.example.music_project.models.Song;
import com.example.music_project.views.adapters.SongAdapter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class AlbumFragment extends Fragment {
    // Khai báo các hằng số cho album, nghệ sĩ và thể loại
    private static final String ARG_ALBUM_ID = "album_id";
    private static final String ARG_ALBUM_NAME = "album_name";
    private static final String ARG_ARTIST_ID = "artist_id"; // Thay đổi thành artist_id
    private static final String ARG_GENRE_ID = "genre_id";

    private AlbumController albumController;
    private ArtistController artistController;
    private GenreController genreController;
    private RecyclerView rvSongs;
    private SongAdapter songAdapter;
    private List<Song> songList = new ArrayList<>();
    private List<Song> fullSongList = new ArrayList<>();
    private DialogEditAlbumFragment dialogEditAlbumFragment;
    private TextView tvAlbumName;
    private ImageView imgAlbumCover;

    // Hàm newInstance để tạo Fragment và truyền tham số (albumId, albumName, artistId, genreId)
    public static AlbumFragment newInstance(int albumId, String albumName, int artistId, int genreId) {
        AlbumFragment fragment = new AlbumFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_ALBUM_ID, albumId);
        args.putString(ARG_ALBUM_NAME, albumName); // Truyền tên album
        args.putInt(ARG_ARTIST_ID, artistId); // Truyền artistId
        args.putInt(ARG_GENRE_ID, genreId); // Truyền genreId
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_album_detail, container, false);

        // Khởi tạo controller và RecyclerView
        albumController = new AlbumController(getContext());
        artistController = new ArtistController(getContext());
        genreController = new GenreController(getContext());
        rvSongs = view.findViewById(R.id.rv_album_songs);
        rvSongs.setLayoutManager(new LinearLayoutManager(getContext()));

        if(getArguments() != null) {
            // Lấy albumId, albumName, artistName và genreName từ arguments và hiển thị
            int albumId = getArguments().getInt(ARG_ALBUM_ID);
            String albumName = getArguments().getString(ARG_ALBUM_NAME);
            int artistId = getArguments().getInt(ARG_ARTIST_ID);
            int genreId = getArguments().getInt(ARG_GENRE_ID);

            imgAlbumCover = view.findViewById(R.id.img_album_square_cover);
            // Hiển thị tên album, nghệ sĩ và thể loại trong TextView
            tvAlbumName = view.findViewById(R.id.tv_album_square_title);
            tvAlbumName.setText(albumName);
            // Tải tên nghệ sĩ và thể loại
            loadArtistName(artistId, view);
            loadGenreName(genreId, view);

            loadAlbumDetails(albumId);
            loadSongsInAlbum(albumId);
            ImageButton btn_setting = view.findViewById(R.id.btn_setting);
            btn_setting.setOnClickListener(v -> showEditAlbumDialog(albumId));

            songAdapter = new SongAdapter(songList, song -> {
                FragmentManager fragmentManager = getParentFragmentManager();
                PlaybackDialogFragment playbackFragment =
                        (PlaybackDialogFragment) fragmentManager.findFragmentById(R.id.player_container);

                if (playbackFragment != null) {
                    playbackFragment.updateSong(song.getSong_id());
                }
            });

            songAdapter.setOnSongLongClickListener(new SongAdapter.OnSongLongClickListener() {
                @Override
                public void onSongLongClick(Song song) {
                    // Hiển thị hộp thoại xác nhận khi nhấn lâu
                    showDeleteSongConfirmationDialog(song);
                }
            });

            rvSongs.setAdapter(songAdapter);

            FloatingActionButton btn_play = view.findViewById(R.id.btn_album_play);
            Button btn_add_song = view.findViewById(R.id.btn_add_song);

            btn_add_song.setOnClickListener(v -> {
//          Create an instance of AddSongToAlbumFragment
                AddSongToAlbumFragment addSongToAlbumFragment = AddSongToAlbumFragment.newInstance(albumId);

//          Replace the current fragment with AddSongToAlbumFragment
                getParentFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, addSongToAlbumFragment) // Replace with your container ID
                        .addToBackStack(null) // Add to back stack so the user can navigate back
                        .commit();
            });

            // Khởi tạo SearchView
            SearchView searchView = view.findViewById(R.id.sv_album_search); // Thay đổi ID nếu cần
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    return false;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    filterSongs(newText); // Gọi phương thức filter từ adapter
                    return true;
                }
            });
        }
        return view;
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

    private void loadAlbumDetails(int albumId) {
        albumController.getAlbumById(albumId, new AlbumController.OnAlbumLoadedListener() {
            @Override
            public void onAlbumLoaded(Album album) {
                if (album != null) {
                    tvAlbumName.setText(album.getTitle());
                    String coverImagePath = album.getCover_image_path();
                    if (coverImagePath != null && !coverImagePath.isEmpty()) {
                        Glide.with(requireContext())
                                .load(coverImagePath)
                                .placeholder(R.drawable.sample_album_cover)
                                .error(R.drawable.default_album_art)
                                .into(imgAlbumCover);
                    } else {
                        imgAlbumCover.setImageResource(R.drawable.sample_album_cover);
                    }
                }
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(getContext(), error,
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Phương thức tải danh sách bài hát trong album
    private void loadSongsInAlbum(int albumId) {
        albumController.getSongsInAlbum(albumId, new AlbumController.OnSongsLoadedListener() {
            @Override
            public void onSongsLoaded(List<Song> songs) {
                if (songs != null && !songs.isEmpty()) {
                    fullSongList.clear();
                    fullSongList.addAll(songs);
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
                        Toast.makeText(getContext(),  error, Toast.LENGTH_SHORT).show();
                    });
                }
            }
        });
    }

    // Method to show the Edit Album dialog
    private void showEditAlbumDialog(int albumId) {
        dialogEditAlbumFragment = DialogEditAlbumFragment.newInstance(albumId);
        dialogEditAlbumFragment.setOnAlbumEditedListener(() -> {
            // Tải lại danh sách bài hát sau khi chỉnh sửa album
            loadAlbumDetails(albumId);
            Toast.makeText(getContext(), "Album đã được chỉnh sửa", Toast.LENGTH_SHORT).show();
        });
        dialogEditAlbumFragment.show(getFragmentManager(), "edit_album");
    }

    // Phương thức tải tên nghệ sĩ
    private void loadArtistName(int artistId, View view) {
        artistController.getArtistById(artistId, new ArtistController.OnArtistLoadedListener() {
            @Override
            public void onArtistLoaded(Artist artist) {
                TextView tvArtist = view.findViewById(R.id.tv_album_artist);
                tvArtist.setText(artist.getArtist_name()); // Hiển thị tên nghệ sĩ
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(getContext(),  error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Phương thức tải tên thể loại
    private void loadGenreName(int genreId, View view) {
        genreController.getGenreById(genreId, new GenreController.OnGenreLoadedListener() {
            @Override
            public void onGenreLoaded(Genre genre) {
                TextView tvGenre = view.findViewById(R.id.tv_album_genre);
                tvGenre.setText(genre.getGenre_name()); // Hiển thị tên thể loại
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(getContext(), "Failed to load genre: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showDeleteSongConfirmationDialog(Song song) {
        int albumId = getArguments().getInt(ARG_ALBUM_ID);
        new AlertDialog.Builder(requireContext())
                .setTitle("Xóa Bài Hát")
                .setMessage("Bạn có chắc chắn muốn xóa bài hát \n \n \"" + song.getTitle() + "\" \n \nkhỏi album không?")
                .setPositiveButton("Có", (dialog, which) -> {
                    deleteSongFromAlbum(song);
                    loadSongsInAlbum(albumId);
                })
                .setNegativeButton("Không", null)
                .show();
    }

    private void deleteSongFromAlbum(Song song) {
        int albumId = getArguments().getInt(ARG_ALBUM_ID);
        albumController.deleteSongFromAlbum(albumId, song.getSong_id(), new AlbumController.OnSongDeletedListener() {
            @Override
            public void onSongDeleted(Song song) {
                Toast.makeText(getContext(), "Đã xóa bài hát khỏi album!", Toast.LENGTH_SHORT).show();
                loadSongsInAlbum(albumId);
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(getContext(), "Lỗi khi xóa bài hát: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
