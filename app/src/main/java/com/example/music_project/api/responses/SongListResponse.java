package com.example.music_project.api.responses;

import com.example.music_project.models.Song;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class SongListResponse {
    @SerializedName("items")
    private List<TrackItem> items;

    public List<TrackItem> getItems() {
        return items;
    }

    public static class TrackItem {
        @SerializedName("track")
        private Song track;

        public Song getTrack() {
            return track;
        }
    }
}