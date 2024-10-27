package com.example.music_project.views.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import com.example.music_project.database.AppDatabase;
import com.example.music_project.database.Seeder;
import com.example.music_project.models.Song;
import com.example.music_project.services.MusicPlaybackService;
import com.example.music_project.views.fragments.PlaybackDialogFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.example.music_project.R;
import com.example.music_project.views.fragments.HomeFragment;
import com.example.music_project.views.fragments.LibraryFragment;
import com.example.music_project.views.fragments.SearchFragment;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private static final String PREF_NAME = "MyAppPreferences13";
    private static final String KEY_IS_FIRST_RUN = "isFirstRun13";

    private BottomNavigationView bottomNav;
    private PlaybackDialogFragment playbackFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeDatabase();
        setupBottomNavigation();
        initializePlaybackFragment(savedInstanceState);
    }

    private void initializeDatabase() {
        SharedPreferences prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        boolean isFirstRun = prefs.getBoolean(KEY_IS_FIRST_RUN, true);

        if (isFirstRun) {
            new Thread(() -> {
                try {
                    AppDatabase database = AppDatabase.getInstance(getApplicationContext());
                    Seeder.seedDatabase(database);

                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putBoolean(KEY_IS_FIRST_RUN, false);
                    editor.apply();
                } catch (Exception e) {
                    Log.e(TAG, "Error seeding database: " + e.getMessage());
                }
            }).start();
        }
    }

    private void setupBottomNavigation() {
        bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int itemId = item.getItemId();

            if (itemId == R.id.nav_home) {
                selectedFragment = new HomeFragment();
            } else if (itemId == R.id.nav_library) {
                selectedFragment = new LibraryFragment();
            } else if (itemId == R.id.nav_search) {
                selectedFragment = new SearchFragment();
            } else {
                return false;
            }

            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, selectedFragment)
                    .commit();

            return true;
        });

        // Set default fragment
        if (getSupportFragmentManager().findFragmentById(R.id.fragment_container) == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new HomeFragment())
                    .commit();
        }
    }

    private void initializePlaybackFragment(Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            playbackFragment = new PlaybackDialogFragment();
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.player_container, playbackFragment)
                    .commit();

            Intent serviceIntent = new Intent(this, MusicPlaybackService.class);
            startService(serviceIntent);
        } else {
            playbackFragment = (PlaybackDialogFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.player_container);
        }
    }

    /**
     * Method to be called by other fragments to play a song
     */
    public void playSong(Song song) {
        if (playbackFragment != null) {
            playbackFragment.updateCurrentSong(song);
        }
    }

    /**
     * Get the currently playing status
     * @return boolean indicating if music is currently playing
     */
    public boolean isPlaying() {
        return playbackFragment != null && playbackFragment.isPlaying();
    }

//    /**
//     * Get the currently playing song
//     * @return Song object that is currently playing, or null if none
//     */
//    public Song getCurrentPlayingSong() {
//        return playbackFragment != null ? playbackFragment.getCurrentSong() : null;
//    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Stop the music service if the activity is being destroyed
        Intent serviceIntent = new Intent(this, MusicPlaybackService.class);
        stopService(serviceIntent);
    }
}