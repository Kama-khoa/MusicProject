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
import com.example.music_project.models.Album;

import java.util.List;

public class AlbumAdapter extends RecyclerView.Adapter<AlbumAdapter.AlbumViewHolder> {
    private List<Album> albums;
    private OnAlbumClickListener listener;

    public AlbumAdapter(List<Album> albums, OnAlbumClickListener listener) {
        this.albums = albums;
        this.listener = listener;
    }

    @NonNull
    @Override
    public AlbumViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_album, parent, false);
        return new AlbumViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AlbumViewHolder holder, int position) {
        Album album = albums.get(position);
        Log.d("AlbumAdapter", "Album Title: " + album.getTitle());
        holder.tvAlbumTitle.setText(album.getTitle());
        holder.tvAlbumReleaseDate.setText(album.getRelease_date().toString());

        // Xử lý sự kiện nhấn vào một album
        holder.itemView.setOnClickListener(v -> listener.onAlbumClick(album));
    }

    @Override
    public int getItemCount() {
        return albums.size();
    }

    public void setAlbums(List<Album> albums) {
        this.albums = albums;
        notifyDataSetChanged();
    }

    public void addAlbum(Album album) {
        albums.add(album);
        notifyItemInserted(albums.size() - 1);
    }

    public void updateAlbums(List<Album> newAlbums) {
        this.albums.clear(); // Xóa danh sách hiện tại
        this.albums.addAll(newAlbums); // Thêm danh sách mới
        notifyDataSetChanged(); // Cập nhật RecyclerView
    }

    public void removeAlbum(int position) {
        if (position >= 0 && position < albums.size()) {
            albums.remove(position);
            notifyItemRemoved(position);
        }
    }

    public interface OnAlbumClickListener {
        void onAlbumClick(Album album);
    }

    static class AlbumViewHolder extends RecyclerView.ViewHolder {
        TextView tvAlbumTitle;
        TextView tvAlbumReleaseDate;
        ImageView imgAlbumCover;

        public AlbumViewHolder(@NonNull View itemView) {
            super(itemView);
            tvAlbumTitle = itemView.findViewById(R.id.tv_album_title);
            tvAlbumReleaseDate = itemView.findViewById(R.id.tv_album_release_date);
            imgAlbumCover = itemView.findViewById(R.id.img_album_cover);
        }
    }
}
