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
import com.example.music_project.models.Artist;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class ArtistAdapter extends RecyclerView.Adapter<ArtistAdapter.ArtistViewHolder>{
    private List<Artist> artists;
    private OnArtistClickListener listener;

    public ArtistAdapter(List<Artist> artists, OnArtistClickListener listener) {
        this.artists = artists;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ArtistViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_artist, parent, false);
        return new ArtistViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ArtistViewHolder holder, int position) {
        Artist artist = artists.get(position);
        holder.bind(artist, listener);
    }

    @Override
    public int getItemCount() {
        return artists.size();
    }

    public void setArtists(List<Artist> artists) {
        this.artists = artists;
        notifyDataSetChanged();
    }

    public void addArtist(Artist artist) {
        artists.add(artist);
        notifyItemInserted(artists.size() - 1);
    }

    public void updateArtists(List<Artist> newArtists) {
        this.artists.clear(); // Xóa danh sách hiện tại
        this.artists.addAll(newArtists); // Thêm danh sách mới
        notifyDataSetChanged(); // Cập nhật RecyclerView
    }

    public void removeArtist(int position) {
        if (position >= 0 && position < artists.size()) {
            artists.remove(position);
            notifyItemRemoved(position);
        }
    }

    public interface OnArtistClickListener {
        void onArtistClick(Artist artist);
    }

    static class ArtistViewHolder extends RecyclerView.ViewHolder {
        TextView tvArtistName;
        TextView tvArtistBio;
        TextView tvArtistDob;
        ImageView imgArtistAvatar;

        public ArtistViewHolder(@NonNull View itemView) {
            super(itemView);
            tvArtistName = itemView.findViewById(R.id.tv_artist_name);
            tvArtistBio = itemView.findViewById(R.id.tv_artist_bio);
            tvArtistDob = itemView.findViewById(R.id.tv_artist_dob);
            imgArtistAvatar = itemView.findViewById(R.id.img_artist_avatar);
        }

        public void bind(Artist artist, OnArtistClickListener listener) {
            Log.d("ArtistAdapter", "Artist Name: " + artist.getArtist_name());
            tvArtistName.setText(artist.getArtist_name());
            // Lấy chỉ câu đầu tiên từ bio
            String bio = artist.getBio();
            String[] sentences = bio.split("\\. "); // Tách các câu bằng dấu chấm và khoảng trắng
            String firstSentence = sentences.length > 0 ? sentences[0] : ""; // Lấy câu đầu tiên

            tvArtistBio.setText(firstSentence); // Hiển thị câu đầu tiên

            SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
            String formattedDob = dateFormat.format(artist.getDate_of_birth()); // Chắc chắn rằng artist có phương thức getDateOfBirth()
            tvArtistDob.setText(formattedDob);

            // Xử lý sự kiện nhấn vào một nghệ sĩ
            itemView.setOnClickListener(v -> listener.onArtistClick(artist));
        }
    }
}
