package com.example.music_project.controllers;

import android.content.Context;
import android.widget.Toast;

import com.example.music_project.database.SongDao;
import com.example.music_project.models.Song;


public class PlayerController {
    private SongDao songDao;

    public PlayerController(SongDao songDao) {
        this.songDao = songDao;
    }

}


