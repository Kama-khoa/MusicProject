package com.example.music_project.api;

import com.example.music_project.models.Song;

import java.util.List;

// Tạo một lớp để phản ánh cấu trúc của dữ liệu trả về
public class TopTracksResponse {
    private List<Song> items; // Giả định rằng các bài hát nằm trong trường "items"

    public List<Song> getItems() {
        return items;
    }
}
