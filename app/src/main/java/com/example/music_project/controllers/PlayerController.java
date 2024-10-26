package com.example.music_project.controllers;

import android.content.Context;
import android.widget.Toast;

import com.example.music_project.database.SongDao;
import com.example.music_project.models.Song;
import com.example.music_project.views.activities.PlayerActivity;

public class PlayerController {
    private SongDao songDao;
    private PlayerActivity playerActivity;

    public PlayerController(SongDao songDao, PlayerActivity playerActivity) {
        this.songDao = songDao;
        this.playerActivity = playerActivity;
    }

}


