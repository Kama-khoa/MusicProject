package com.example.music_project.views.adapters;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.music_project.R;
import com.example.music_project.models.Playlist;

import java.util.List;

public class PlaylistAdapter extends RecyclerView.Adapter<PlaylistAdapter.PlaylistViewHolder> {
    private List<Playlist> playlists;
    private OnPlaylistClickListener listener;

    public PlaylistAdapter(List<Playlist> playlists, OnPlaylistClickListener listener) {
        this.playlists = playlists;
        this.listener = listener;
    }

    @NonNull
    @Override
    public PlaylistViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_playlist, parent, false);
        return new PlaylistViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PlaylistViewHolder holder, int position) {
        Playlist playlist = playlists.get(position);
        Log.d("PlaylistAdapter", "Playlist Name: " + playlist.getTitle());
        holder.tvPlaylistName.setText(playlist.getTitle());
        holder.itemView.setOnClickListener(v -> listener.onPlaylistClick(playlist));
    }

    @Override
    public int getItemCount() {
        return playlists.size();
    }

    public void setPlaylists(List<Playlist> playlists) {
        this.playlists = playlists;
        notifyDataSetChanged();
    }

    public void addPlaylist(Playlist playlist) {
        playlists.add(playlist);
        notifyItemInserted(playlists.size() - 1);
    }

    public void updatePlaylists(List<Playlist> newPlaylists) {
        this.playlists.clear(); // Xóa danh sách hiện tại
        this.playlists.addAll(newPlaylists); // Thêm danh sách mới
        notifyDataSetChanged(); // Cập nhật RecyclerView
    }

    public void removePlaylist(int position) {
        if (position >= 0 && position < playlists.size()) {
            playlists.remove(position);
            notifyItemRemoved(position);
        }
    }

    public interface OnPlaylistClickListener {
        void onPlaylistClick(Playlist playlist);
    }

    static class PlaylistViewHolder extends RecyclerView.ViewHolder {
        TextView tvPlaylistName;
        ImageView imgPlaylist;

        public PlaylistViewHolder(@NonNull View itemView) {
            super(itemView);
            tvPlaylistName = itemView.findViewById(R.id.tv_playlist_name);
            imgPlaylist = itemView.findViewById(R.id.img_playlist);
        }
    }
}