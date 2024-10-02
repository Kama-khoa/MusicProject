package com.example.music_project.views.adapters;

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
import java.util.List;

public class SongAdapter extends RecyclerView.Adapter<SongAdapter.SongViewHolder> {
    private List<Song> songs;
    private OnSongClickListener listener;

    public interface OnSongClickListener {
        void onSongClick(Song song);
    }

    public SongAdapter(List<Song> songs, OnSongClickListener listener) {
        this.songs = songs;
        this.listener = listener;
    }

    @NonNull
    @Override
    public SongViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return null;
    }

    @NonNull
    @Override
    public void onBindViewHolder(@NonNull SongViewHolder holder, int position) {
        Song song = songs.get(position);
//        holder.bind(song);
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
                    listener.onSongClick(songs.get(position));
                }
            });
        }

//        void bind(Song song) {
//            tvTitle.setText(song.getTitle());
//            tvArtist.setText(song.getArtist());
//            if (song.getCoverUrl() != null && !song.getCoverUrl().isEmpty()) {
//                Glide.with(itemView.getContext())
//                        .load(song.getCoverUrl())
//                        .placeholder(R.drawable.default_song_cover)
//                        .error(R.drawable.default_song_cover)
//                        .into(ivCover);
//            } else {
//                ivCover.setImageResource(R.drawable.default_song_cover);
//            }
//        }
    }


    @Override
    public int getItemCount() {
        return songs.size();
    }

    public void updateSongs(List<Song> newSongs) {
        this.songs = newSongs;
        notifyDataSetChanged();
    }


}