package com.example.music_project.models;

public class AlbumWithDetails {
    private Album album;
    private String artistName;
    private String genreName;

    public AlbumWithDetails(Album album, String artistName, String genreName) {
        this.album = album;
        this.artistName = artistName;
        this.genreName = genreName;
    }

    public Album getAlbum() {
        return album;
    }

    public String getArtistName() {
        return artistName;
    }

    public String getGenreName() {
        return genreName;
    }
}
