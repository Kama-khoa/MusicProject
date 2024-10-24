package com.example.music_project.views.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.music_project.R;
import com.example.music_project.models.AlbumWithDetails;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class AlbumWithDetailsAdapter extends RecyclerView.Adapter<AlbumWithDetailsAdapter.AlbumWithDetailsViewHolder> {
    private List<AlbumWithDetails> albumsWithDetails;
    private OnAlbumWithDetailsClickListener listener;
    private OnAlbumWithDetailsLongClickListener onAlbumLongClickListener;

    public AlbumWithDetailsAdapter(List<AlbumWithDetails> albumsWithDetails, OnAlbumWithDetailsClickListener listener, OnAlbumWithDetailsLongClickListener onAlbumLongClickListener) {
        this.albumsWithDetails = albumsWithDetails;
        this.listener = listener;
        this.onAlbumLongClickListener = onAlbumLongClickListener;
    }

    @NonNull
    @Override
    public AlbumWithDetailsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_album, parent, false);
        return new AlbumWithDetailsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AlbumWithDetailsViewHolder holder, int position) {
        AlbumWithDetails albumWithDetails = albumsWithDetails.get(position);
        holder.bind(albumWithDetails, listener, onAlbumLongClickListener);
    }

    @Override
    public int getItemCount() {
        return albumsWithDetails.size();
    }

    public void setAlbumsWithDetails(List<AlbumWithDetails> albumsWithDetails) {
        this.albumsWithDetails = albumsWithDetails;
        notifyDataSetChanged();
    }

    public interface OnAlbumWithDetailsClickListener {
        void onAlbumWithDetailsClick(AlbumWithDetails albumWithDetails);
    }

    static class AlbumWithDetailsViewHolder extends RecyclerView.ViewHolder {
        TextView tvAlbumTitle;
        TextView tvAlbumArtist;
        TextView tvAlbumReleaseDate;
        ImageView imgAlbumCover;

        public AlbumWithDetailsViewHolder(@NonNull View itemView) {
            super(itemView);
            tvAlbumTitle = itemView.findViewById(R.id.tv_album_title);
            tvAlbumArtist = itemView.findViewById(R.id.tv_album_artist);
            tvAlbumReleaseDate = itemView.findViewById(R.id.tv_album_release_date);
            imgAlbumCover = itemView.findViewById(R.id.img_album_cover);
        }

        public void bind(AlbumWithDetails albumWithDetails, OnAlbumWithDetailsClickListener listener, OnAlbumWithDetailsLongClickListener longClickListener) {
            tvAlbumTitle.setText(albumWithDetails.getAlbum().getTitle());
            tvAlbumArtist.setText(albumWithDetails.getArtistName()); // Giả sử AlbumWithDetails chứa artistName
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
            String formattedDate = dateFormat.format(albumWithDetails.getAlbum().getRelease_date());
            tvAlbumReleaseDate.setText(formattedDate);

            itemView.setOnClickListener(v -> listener.onAlbumWithDetailsClick(albumWithDetails));

            itemView.setOnLongClickListener(v -> {
                longClickListener.onAlbumLongClick(albumWithDetails);
                return true;
            });
        }
    }

    public interface OnAlbumWithDetailsLongClickListener {
        void onAlbumLongClick(AlbumWithDetails albumWithDetails);
    }
}
