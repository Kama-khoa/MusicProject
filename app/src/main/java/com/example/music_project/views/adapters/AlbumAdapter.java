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

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

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
        holder.bind(album, listener);
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
        TextView tvAlbumArtist;
        TextView tvAlbumReleaseDate;
        ImageView imgAlbumCover;

        public AlbumViewHolder(@NonNull View itemView) {
            super(itemView);
            tvAlbumTitle = itemView.findViewById(R.id.tv_album_title);
            tvAlbumArtist = itemView.findViewById(R.id.tv_album_artist);
            tvAlbumReleaseDate = itemView.findViewById(R.id.tv_album_release_date);
            imgAlbumCover = itemView.findViewById(R.id.img_album_cover);
        }
        public void bind(Album album, OnAlbumClickListener listener){
            Log.d("AlbumAdapter", "Album Title: " + album.getTitle());
            tvAlbumTitle.setText(album.getTitle());
            tvAlbumArtist.setText(String.valueOf(album.getArtist_id()));
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
            String formattedDate = dateFormat.format(album.getRelease_date());
            tvAlbumReleaseDate.setText(formattedDate);

            // Xử lý sự kiện nhấn vào một album
            itemView.setOnClickListener(v -> listener.onAlbumClick(album));
        }
    }
}
