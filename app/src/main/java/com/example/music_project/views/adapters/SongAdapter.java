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
import java.util.List;

public class SongAdapter extends RecyclerView.Adapter<SongAdapter.SongViewHolder> {
    private List<Song> songs;
    private List<Song> songsFull;
    private OnSongClickListener listener;
    private OnSongLongClickListener longClickListener;
    private List<Song> selectedSongs = new ArrayList<>();

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
    }

    // Phương thức để thiết lập OnSongLongClickListener
    public void setOnSongLongClickListener(OnSongLongClickListener listener) {
        this.longClickListener = listener;
    }

    public List<Song> getSelectedSongs() {
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

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    Song clickedSong = songs.get(position);
                    if (selectedSongs.contains(clickedSong)) {
                        selectedSongs.remove(clickedSong); // Nếu bài hát đã được chọn, bỏ chọn nó
                        itemView.setBackgroundColor(Color.WHITE); // Thay đổi màu nền để thể hiện trạng thái không chọn
                    } else {
                        selectedSongs.add(clickedSong); // Chọn bài hát
                        itemView.setBackgroundColor(Color.LTGRAY); // Thay đổi màu nền để thể hiện trạng thái được chọn
                    }
                    listener.onSongClick(clickedSong); // Gọi lại callback khi người dùng click
                }
            });

            itemView.setOnLongClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && longClickListener != null) {
                    Song longClickedSong = songs.get(position);
                    longClickListener.onSongLongClick(longClickedSong); // Gọi lại callback khi người dùng nhấn lâu
                }
                return true;
            });

        }

        void bind(Song song) {
            tvTitle.setText(song.getTitle());
            tvArtist.setText(song.getArtistName());
            // Tạm thời bỏ qua phần load ảnh cover
            ivCover.setImageResource(R.drawable.default_song_cover);
        }
    }

    @Override
    public int getItemCount() {
        return songs.size();
    }

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
                if (song.getTitle().toLowerCase().contains(query.toLowerCase())) { // Tìm kiếm theo chữ cái xuất hiện trong tiêu đề
                    filteredList.add(song);
                }
            }
            songs = filteredList; // Cập nhật danh sách với các bài hát đã lọc
        }
        notifyDataSetChanged(); // Cập nhật RecyclerView
    }


}