package com.example.music_project.views.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.content.SharedPreferences;
import android.os.Bundle;

import com.example.music_project.database.AppDatabase;
import com.example.music_project.database.Seeder;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.example.music_project.R;
import com.example.music_project.views.fragments.HomeFragment;
import com.example.music_project.views.fragments.LibraryFragment;
import com.example.music_project.views.fragments.SearchFragment;

public class MainActivity extends AppCompatActivity {
    private BottomNavigationView bottomNav;
    private static final String PREF_NAME = "MyAppPreferences5";
    private static final String KEY_IS_FIRST_RUN = "isFirstRun5";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SharedPreferences prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        boolean isFirstRun = prefs.getBoolean(KEY_IS_FIRST_RUN, true);

        if (isFirstRun) {
            new Thread(() -> {
                AppDatabase database = AppDatabase.getInstance(getApplicationContext());
                Seeder.seedDatabase(database);
            }).start();


            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean(KEY_IS_FIRST_RUN, false);
            editor.apply();
        }
        bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnNavigationItemSelectedListener(navListener);

        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                new HomeFragment()).commit();
    }

    private BottomNavigationView.OnNavigationItemSelectedListener navListener =
            item -> {
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

                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        selectedFragment).commit();

                return true;
            };
}
