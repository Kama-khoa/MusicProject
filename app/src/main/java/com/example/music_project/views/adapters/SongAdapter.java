package com.example.music_project.views.adapters;

import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
//import com.bumptech.glide.Glide;
import com.bumptech.glide.Glide;
import com.example.music_project.R;
import com.example.music_project.controllers.SongController;
import com.example.music_project.database.AppDatabase;
import com.example.music_project.database.SongImageDao;
import com.example.music_project.models.Song;
import com.example.music_project.models.SongImage;
import com.example.music_project.views.fragments.LoadSongImage;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SongAdapter extends RecyclerView.Adapter<SongAdapter.SongViewHolder> {
    private List<Song> songs; // Danh sách bài hát để hiển thị
    private List<Song> songsFull; // Danh sách đầy đủ để tìm kiếm
    private OnSongClickListener listener; // Listener cho sự kiện nhấp chuột
    private OnSongLongClickListener longClickListener; // Listener cho sự kiện nhấn giữ
    private Set<Integer> selectedSongIds = new HashSet<>();

    private SongImageDao songImageDao;

    private AppDatabase appDatabase;

    public interface OnSongClickListener {
        void onSongClick(Song song);
    }

    public interface OnSongLongClickListener {
        void onSongLongClick(Song song);
    }

    public SongAdapter(List<Song> songs, OnSongClickListener listener) {
        this.songs = songs;
        this.songsFull = new ArrayList<>(songs);
        this.listener = listener;
    }

    @NonNull
    @Override
    public SongViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_song, parent, false);
        return new SongViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SongViewHolder holder, int position) {
        Song song = songs.get(position);
        holder.bind(song);

        if (selectedSongIds.contains(song.getSong_id())) {
            holder.itemView.setBackgroundColor(Color.LTGRAY);
        } else {
            holder.itemView.setBackgroundColor(Color.WHITE); 
        }
    }

    public void setOnSongLongClickListener(OnSongLongClickListener listener) {
        this.longClickListener = listener;
    }

    public List<Song> getSelectedSongs() {
        List<Song> selectedSongs = new ArrayList<>();
        for (Song song : songs) {
            if (selectedSongIds.contains(song.getSong_id())) {
                selectedSongs.add(song);
            }
        }
        return selectedSongs;
    }

    class SongViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvArtist;
        ImageView ivCover, ivOptions;

        SongViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_song_title);
            tvArtist = itemView.findViewById(R.id.tv_artist);
            ivCover = itemView.findViewById(R.id.iv_song_cover);
            ivOptions = itemView.findViewById(R.id.img_song_edit_or_del);

            songImageDao = AppDatabase.getInstance(itemView.getContext()).songImageDao();

            itemView.setOnClickListener(v -> {
                int position =  getBindingAdapterPosition();;
                if (position != RecyclerView.NO_POSITION) {
                    Song clickedSong = songs.get(position);
                    if (selectedSongIds.contains(clickedSong.getSong_id())) {
                        selectedSongIds.remove(clickedSong.getSong_id());
                        itemView.setBackgroundColor(Color.WHITE);
                    } else {
                        selectedSongIds.add(clickedSong.getSong_id());
                        itemView.setBackgroundColor(Color.LTGRAY);
                    }
                    listener.onSongClick(clickedSong);
                }
            });

            itemView.setOnLongClickListener(v -> {
                int position =  getBindingAdapterPosition();;
                if (position != RecyclerView.NO_POSITION && longClickListener != null) {
                    Song longClickedSong = songs.get(position);
                    longClickListener.onSongLongClick(longClickedSong);
                }
                return true;
            });
        }

        void bind(Song song) {
            tvTitle.setText(song.getTitle());
            tvArtist.setText(song.getArtistName());

            String imagePath = song.getImg_path();

            if (imagePath != null) {

                if (imagePath.startsWith("res/")) {
                    int resourceId = ivCover.getResources().getIdentifier(
                            imagePath.replace("res/raw/", "").replace(".png", ""),
                            "raw",
                            ivCover.getContext().getPackageName());

                    Glide.with( ivCover.getContext())
                            .load(resourceId)
                            .into(ivCover);
                } else {
                    Glide.with( ivCover.getContext())
                            .load(imagePath)
                            .into(ivCover);
                }
            } else {
                ivCover.setImageResource(R.drawable.ic_image_playlist);
            }

        }

    }

    @Override
    public int getItemCount() {
        return songs.size();
    }

    // Cập nhật danh sách bài hát mới
    public void updateSongs(List<Song> newSongs) {
        this.songs = newSongs;
        notifyDataSetChanged();
    }

    // Phương thức tìm kiếm
    public void filter(String query) {
        if (query.isEmpty()) {
            songs = new ArrayList<>(songsFull); // Nếu không có gì được nhập, hiển thị tất cả
        } else {
            List<Song> filteredList = new ArrayList<>();
            for (Song song : songsFull) {
                if (song.getTitle().toLowerCase().contains(query.toLowerCase())) { // Tìm kiếm theo tiêu đề
                    filteredList.add(song);
                }
            }
            songs = filteredList;
        }
        notifyDataSetChanged();
    }
}
