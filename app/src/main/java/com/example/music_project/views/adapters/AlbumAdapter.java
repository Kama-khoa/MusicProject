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
import com.example.music_project.models.Album;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class AlbumAdapter extends RecyclerView.Adapter<AlbumAdapter.AlbumViewHolder> {
    private List<Album> albums;
    private OnAlbumClickListener listener;
    private OnAlbumLongClickListener onAlbumLongClickListener;

    public AlbumAdapter(List<Album> albumList, OnAlbumClickListener onAlbumClickListener, OnAlbumLongClickListener onAlbumLongClickListener) {
        this.albums = albumList;
        this.listener = onAlbumClickListener;
        this.onAlbumLongClickListener = onAlbumLongClickListener;
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
        holder.bind(album, listener, onAlbumLongClickListener);
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
        this.albums.clear();
        this.albums.addAll(newAlbums);
        notifyDataSetChanged();
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
        public void bind(Album album, OnAlbumClickListener listener, OnAlbumLongClickListener longClickListener){
            Log.d("AlbumAdapter", "Album Title: " + album.getTitle());
            tvAlbumTitle.setText(album.getTitle());
            tvAlbumArtist.setText(String.valueOf(album.getArtist_id()));
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
            String formattedDate = dateFormat.format(album.getRelease_date());
            tvAlbumReleaseDate.setText(formattedDate);
            Glide.with(itemView.getContext())
                    .load(album.getCover_image_path())
                    .placeholder(R.drawable.sample_album_cover)
                    .error(R.drawable.default_album_art)
                    .into(imgAlbumCover);

            itemView.setOnClickListener(v -> listener.onAlbumClick(album));

            itemView.setOnLongClickListener(v -> {
                longClickListener.onAlbumLongClick(album);
                return true;
            });
        }
    }
    public interface OnAlbumLongClickListener {
        void onAlbumLongClick(Album album);
    }
}