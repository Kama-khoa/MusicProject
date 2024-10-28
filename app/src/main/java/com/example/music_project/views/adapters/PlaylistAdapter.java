package com.example.music_project.views.adapters;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
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
        if (playlist != null) {
            // Kiểm tra tên và đường dẫn ảnh có null không
            String title = playlist.getTitle();
            String imagePath = playlist.getImageResource();

            if (title != null) {
                holder.tvPlaylistName.setText(title);
            } else {
                holder.tvPlaylistName.setText("Tên playlist không có sẵn");
            }

            if (imagePath != null) {
                // Xử lý việc tải ảnh từ imagePath ở đây
                if (imagePath.startsWith("res/")) {
                    int resourceId = holder.imgPlaylist.getResources().getIdentifier(
                            imagePath.replace("res/raw/", "").replace(".png", ""),
                            "raw",
                            holder.imgPlaylist.getContext().getPackageName());

                    Glide.with( holder.imgPlaylist.getContext())
                            .load(resourceId)
                            .into(holder.imgPlaylist);
                } else {
                    Glide.with( holder.imgPlaylist.getContext())
                            .load(imagePath)
                            .into(holder.imgPlaylist);
                }
            } else {
                holder.imgPlaylist.setImageResource(R.drawable.ic_image_playlist); // Ảnh mặc định
            }
        } else {
            // Xử lý trường hợp playlist null
            holder.tvPlaylistName.setText("Playlist không hợp lệ");
            holder.imgPlaylist.setImageResource(R.drawable.ic_image_playlist); // Ảnh mặc định
        }
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