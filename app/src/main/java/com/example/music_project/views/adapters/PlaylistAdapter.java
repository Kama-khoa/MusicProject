package com.example.music_project.views.adapters;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.music_project.R;
import com.example.music_project.models.Playlist;

import java.util.List;

public class PlaylistAdapter extends BaseAdapter {

    private Context context;
    private List<Playlist> playlists;

    public PlaylistAdapter(Context context, List<Playlist> playlists) {
        this.context = context;
        this.playlists = playlists;
    }

    @Override
    public int getCount() {
        return playlists.size();
    }

    @Override
    public Object getItem(int position) {
        return playlists.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_playlist, parent, false);
        }

        Playlist playlist = playlists.get(position);

        ImageView imgPlaylist = convertView.findViewById(R.id.img_playlist);
        TextView tvPlaylistName = convertView.findViewById(R.id.tv_playlist_name);
        TextView tvPlaylistDetails = convertView.findViewById(R.id.tv_playlist_details);

        // Gán dữ liệu cho các view
        imgPlaylist.setImageResource(playlist.getImageResource());
        tvPlaylistName.setText(playlist.getTitle());
        tvPlaylistDetails.setText(playlist.getDetails());

        return convertView;
    }
}