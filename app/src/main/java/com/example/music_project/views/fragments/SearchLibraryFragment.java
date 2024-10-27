package com.example.music_project.views.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.music_project.R;
import com.example.music_project.controllers.AlbumController;
import com.example.music_project.controllers.ArtistController;
import com.example.music_project.controllers.GenreController;
import com.example.music_project.controllers.PlaylistController;
import com.example.music_project.models.Album;
import com.example.music_project.models.AlbumWithDetails;
import com.example.music_project.models.Artist;
import com.example.music_project.models.Genre;
import com.example.music_project.models.Playlist;
import com.example.music_project.views.adapters.AlbumAdapter;
import com.example.music_project.views.adapters.AlbumWithDetailsAdapter;
import com.example.music_project.views.adapters.PlaylistAdapter;

import java.util.ArrayList;
import java.util.List;

public class SearchLibraryFragment extends Fragment {
    private SearchView searchView;
    private Button btnCancel, btnPlaylist, btnAlbum, btnArtist;
    private RecyclerView rvSearchResults;
    private AlbumController albumController;
    private PlaylistController playlistController;
    private ArtistController artistController;
    private GenreController genreController;

    private AlbumAdapter albumAdapter;
    private PlaylistAdapter playlistAdapter;

    private List<Album> albumList = new ArrayList<>();
    private List<Playlist> playlistList = new ArrayList<>();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search_library, container, false);

        searchView = view.findViewById(R.id.search_view);
        btnCancel = view.findViewById(R.id.btn_cancel_search);
        btnPlaylist = view.findViewById(R.id.btn_search_playlist);
        btnAlbum = view.findViewById(R.id.btn_search_album);
        btnArtist = view.findViewById(R.id.btn_search_artist);
        rvSearchResults = view.findViewById(R.id.rv_library_items);
        rvSearchResults.setLayoutManager(new LinearLayoutManager(getContext()));

        albumController = new AlbumController(getContext());
        playlistController = new PlaylistController(getContext());
        artistController = new ArtistController(getContext());
        genreController = new GenreController(getContext());

        setupListeners();
        return view;
    }

    private void setupListeners() {
        // Thiết lập nút Hủy để quay lại trang trước
        btnCancel.setOnClickListener(v -> getActivity().onBackPressed());
        // Thiết lập SearchView để tìm kiếm album và playlist
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // Không cần tìm kiếm khi nhấn Enter
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // Tìm kiếm tự động khi người dùng nhập từ khóa
                if (newText.isEmpty()) {
                    clearSearchResults();
                } else {
                    // Gọi hàm tìm kiếm cho album và playlist khi có ký tự mới
                    search(newText);
                }
                return true; // Trả về true nếu bạn xử lý sự kiện
            }
        });
    }

    private void search(String query) {
        // Tìm kiếm playlist trước
        searchPlaylists(query);
        searchAlbums(query);
    }

    private void searchAlbums(String query) {
        // Tìm kiếm album
        albumController.searchAlbums(query, new AlbumController.OnAlbumsLoadedListener() {
            @Override
            public void onAlbumsLoaded(List<Album> albums) {
                Log.d("SearchFragment", "Albums loaded: " + albums.size());
                if (albums != null && !albums.isEmpty()) {
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
            }
            @Override
            public void onFailure(String error){
                new Handler(Looper.getMainLooper()).post(() -> Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void searchPlaylists(String query) {
        // Tìm kiếm playlist
        playlistController.searchPlaylists(query, new PlaylistController.OnPlaylistsLoadedListener() {
            @Override
            public void onPlaylistsLoaded(List<Playlist> playlists) {
                Log.d("SearchFragment", "Playlists loaded: " + playlists.size());
                if (playlists != null && !playlists.isEmpty()) {
                    playlistList.clear();
                    playlistList.addAll(playlists);
                    if (playlistAdapter == null) {
                        playlistAdapter = new PlaylistAdapter(playlistList, playlist -> {
                            // Chuyển tới màn hình chi tiết playlist
                            openPlaylistDetails(playlist);
                        });
                        rvSearchResults.setAdapter(playlistAdapter);
                    } else {
                        playlistAdapter.notifyDataSetChanged();
                    }
                } else {
                    Log.d("SearchFragment", "No playlists found.");
                    // Gọi hàm tìm kiếm album nếu không tìm thấy playlist
                    //searchAlbums(query);
                }
            }

            @Override
            public void onFailure(String error) {
                showToast(error);
            }
        });
    }


    private void clearSearchResults() {
        albumList.clear();
        playlistList.clear();
        if (albumAdapter != null) {
            albumAdapter.notifyDataSetChanged();
        }
        if (playlistAdapter != null) {
            playlistAdapter.notifyDataSetChanged();
        }
    }

    private void openPlaylistDetails(Playlist playlist) {
        // Chuyển tới màn hình chi tiết playlist
        DetailPlaylistFragment detailPlaylistFragment = DetailPlaylistFragment.newInstance(playlist.getPlaylist_id(), playlist.getTitle(), ""); // Cần thêm tên người dùng nếu cần
        getActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, detailPlaylistFragment)
                .addToBackStack(null)
                .commit();
    }

    private void showToast(String message) {
        new Handler(Looper.getMainLooper()).post(() -> Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show());
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

            rvSearchResults.setAdapter(albumWithDetailsAdapter);
        });
    }

    private void loadAlbumDetail(int albumId, String albumName, int artistName, int genreName) {
        AlbumFragment albumDetailFragment = AlbumFragment.newInstance(albumId, albumName, artistName, genreName);
        getActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, albumDetailFragment)
                .addToBackStack(null)
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
