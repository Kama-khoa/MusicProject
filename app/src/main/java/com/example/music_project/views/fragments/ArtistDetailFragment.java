package com.example.music_project.views.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.music_project.R;
import com.example.music_project.controllers.ArtistController;
import com.example.music_project.controllers.GenreController;
import com.example.music_project.models.Album;
import com.example.music_project.models.AlbumWithDetails;
import com.example.music_project.models.Artist;
import com.example.music_project.models.Genre;
import com.example.music_project.models.Song;
import com.example.music_project.views.adapters.AlbumAdapter;
import com.example.music_project.views.adapters.AlbumWithDetailsAdapter;
import com.example.music_project.views.adapters.SongAdapter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ArtistDetailFragment extends Fragment {

    private ImageView ivArtistAvatar;
    private TextView tvArtistName, tvArtistBio, tvDob;
    private RecyclerView rvSongs, rvAlbums;

    public static ArtistDetailFragment newInstance(int artistId, String artistName, String bio, Date dateOfBirth) {
        ArtistDetailFragment fragment = new ArtistDetailFragment();
        Bundle args = new Bundle();
        args.putInt("artistId", artistId);
        args.putString("artistName", artistName);
        args.putString("bio", bio);
        args.putSerializable("dateOfBirth", dateOfBirth);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate layout cho fragment
        View view = inflater.inflate(R.layout.fragment_artist_detail, container, false);

        // Khởi tạo các view
        ivArtistAvatar = view.findViewById(R.id.ivArtistAvatar);
        tvArtistName = view.findViewById(R.id.tvArtistName);
        tvArtistBio = view.findViewById(R.id.tvArtistBio);
        tvDob = view.findViewById(R.id.tv_artist_detail_dob);
        rvSongs = view.findViewById(R.id.rvSongs);
        rvAlbums = view.findViewById(R.id.rvAlbums);

        // Khởi tạo RecyclerView và layout manager cho bài hát và album
        rvSongs.setLayoutManager(new LinearLayoutManager(getContext()));
        rvAlbums.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));

        // Lấy dữ liệu từ arguments và hiển thị
        if (getArguments() != null) {
            int artistId = getArguments().getInt("artistId");
            String artistName = getArguments().getString("artistName");
            String bio = getArguments().getString("bio");
            Date dateOfBirth = (Date) getArguments().getSerializable("dateOfBirth");

            SimpleDateFormat outputFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()); // Định dạng đầu ra

            // Hiển thị thông tin nghệ sĩ
            tvArtistName.setText(artistName);
            tvArtistBio.setText(bio);
            tvDob.setText(outputFormat.format(dateOfBirth));

            // Có thể dùng thư viện Glide hoặc Picasso để load ảnh nghệ sĩ nếu có
            // Glide.with(getContext()).load(artistAvatarUrl).into(ivArtistAvatar);

            // Tải danh sách bài hát và album của nghệ sĩ
            loadSongsAndAlbums(artistId);
        }

        return view;
    }


    private void loadSongsAndAlbums(int artistId) {
        ArtistController artistController = new ArtistController(getContext());

        // Load danh sách bài hát
        artistController.getArtistSongs(artistId, new ArtistController.OnSongsLoadedListener() {
            @Override
            public void onSongsLoaded(List<Song> songs) {
                SongAdapter songAdapter = new SongAdapter(songs, new SongAdapter.OnSongClickListener() {
                    @Override
                    public void onSongClick(Song song) {
                        new Handler(Looper.getMainLooper()).post(() -> Toast.makeText(getContext(), "Bạn đã chọn song " , Toast.LENGTH_SHORT).show());
                    }
                });
                rvSongs.setAdapter(songAdapter);
            }

            @Override
            public void onFailure(String error) {
                new Handler(Looper.getMainLooper()).post(() -> Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show());
            }
        });

        // Fetch artist name first, then load albums
        artistController.getArtistById(artistId, new ArtistController.OnArtistLoadedListener() {
            @Override
            public void onArtistLoaded(Artist artist) {
                // Load danh sách album sau khi đã có thông tin nghệ sĩ
                artistController.getArtistAlbums(artistId, new ArtistController.OnAlbumsLoadedListener() {
                    @Override
                    public void onAlbumsLoaded(List<Album> albums) {
                        // Create a list to hold AlbumWithDetails
                        List<AlbumWithDetails> albumDetailsList = new ArrayList<>();

                        // Load each album's details
                        for (Album album : albums) {

                            AlbumWithDetails details = new AlbumWithDetails(album, artist.getArtist_name(), String.valueOf(album.getGenre_id()));
                            albumDetailsList.add(details);
                            // Update adapter after all details are loaded
                            if (albumDetailsList.size() == albums.size())
                                updateAdapter(albumDetailsList); // Ensure you have this method to update the adapter
                        }
                    }

                    @Override
                    public void onFailure(String error) {
                        new Handler(Looper.getMainLooper()).post(() ->
                                Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show());
                    }
                });
            }

            @Override
            public void onFailure(String error) {
                new Handler(Looper.getMainLooper()).post(() ->
                        Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show());
            }
        });
    }

    // Cập nhật adapter với danh sách AlbumWithDetails
    private void updateAdapter(List<AlbumWithDetails> albumDetailsList) {
        // Chạy trên luồng chính để cập nhật adapter
        new Handler(Looper.getMainLooper()).post(() -> {
            // Cập nhật adapter với albumDetailsList
            AlbumWithDetailsAdapter albumWithDetailsAdapter = new AlbumWithDetailsAdapter(albumDetailsList,
                    albumWithDetails -> loadAlbumDetailFragment(albumWithDetails.getAlbum().getAlbum_id(),
                            albumWithDetails.getAlbum().getTitle(),
                            albumWithDetails.getAlbum().getArtist_id(),
                            albumWithDetails.getAlbum().getGenre_id()),
                    albumWithDetails -> showEditAlbumDialog(albumWithDetails.getAlbum()));  // Handle long press event

            rvAlbums.setAdapter(albumWithDetailsAdapter);
        });
    }

    private void loadAlbumDetailFragment(int albumId, String albumTitle, int artistId, int genreId) {
        // Khởi tạo Fragment album
        AlbumFragment albumFragment = AlbumFragment.newInstance(albumId, albumTitle, artistId, genreId);

        // Thay thế Fragment hiện tại bằng AlbumFragment
        getActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, albumFragment)  // Thay R.id.fragment_container bằng id container của bạn
                .addToBackStack(null)  // Thêm vào back stack để có thể quay lại
                .commit();
    }

    private void showEditAlbumDialog(Album album) {
        DialogEditAlbumFragment dialogEditAlbumFragment = DialogEditAlbumFragment.newInstance(album.getAlbum_id());
        dialogEditAlbumFragment.setOnAlbumEditedListener(() -> {
            Toast.makeText(getContext(), "Album đã được chỉnh sửa", Toast.LENGTH_SHORT).show();
        });
        dialogEditAlbumFragment.show(getFragmentManager(), "edit_album");
    }
}

