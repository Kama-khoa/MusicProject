package com.example.music_project.views.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
//import com.bumptech.glide.Glide;
import com.example.music_project.R;
import com.example.music_project.models.Song;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SongAdapter extends RecyclerView.Adapter<SongAdapter.SongViewHolder> {
    private List<Song> songs; // Danh sách bài hát để hiển thị
    private List<Song> songsFull; // Danh sách đầy đủ để tìm kiếm
    private OnSongClickListener listener; // Listener cho sự kiện nhấp chuột
    private OnSongLongClickListener longClickListener; // Listener cho sự kiện nhấn giữ
    private Set<Integer> selectedSongIds = new HashSet<>(); // Set để theo dõi trạng thái chọn của từng bài hát dựa trên ID

    // Giao diện cho sự kiện nhấn vào bài hát
    public interface OnSongClickListener {
        void onSongClick(Song song);
    }

    // Giao diện cho sự kiện nhấn giữ vào bài hát
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

        // Kiểm tra xem bài hát có đang được chọn hay không và cập nhật màu nền
        if (selectedSongIds.contains(song.getSong_id())) {
            holder.itemView.setBackgroundColor(Color.LTGRAY); // Đã chọn
        } else {
            holder.itemView.setBackgroundColor(Color.WHITE); // Không được chọn
        }
    }

    // Phương thức để thiết lập OnSongLongClickListener
    public void setOnSongLongClickListener(OnSongLongClickListener listener) {
        this.longClickListener = listener;
    }

    // Trả về danh sách các bài hát đã chọn
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
        ImageView ivCover;

        SongViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_song_title);
            tvArtist = itemView.findViewById(R.id.tv_artist);
            ivCover = itemView.findViewById(R.id.iv_song_cover);

            // Sự kiện nhấn vào bài hát
            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    Song clickedSong = songs.get(position);
                    if (selectedSongIds.contains(clickedSong.getSong_id())) {
                        selectedSongIds.remove(clickedSong.getSong_id()); // Bỏ chọn
                        itemView.setBackgroundColor(Color.WHITE); // Trạng thái không chọn
                    } else {
                        selectedSongIds.add(clickedSong.getSong_id()); // Thêm vào danh sách đã chọn
                        itemView.setBackgroundColor(Color.LTGRAY); // Trạng thái đã chọn
                    }
                    listener.onSongClick(clickedSong);
                }
            });

            // Sự kiện nhấn giữ vào bài hát
            itemView.setOnLongClickListener(v -> {
                int position = getAdapterPosition();
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
            ivCover.setImageResource(R.drawable.default_song_cover);
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
