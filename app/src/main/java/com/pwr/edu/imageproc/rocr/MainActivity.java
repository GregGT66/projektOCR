package com.pwr.edu.imageproc.rocr;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.os.Bundle;
import android.view.MenuItem;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.pwr.edu.imageproc.rocr.fragments.CameraFragment;
import com.pwr.edu.imageproc.rocr.fragments.GalleryFragment;

public class MainActivity extends AppCompatActivity {

    private final FragmentManager fragmentManager = getSupportFragmentManager();
    private final Fragment cameraFragment = new CameraFragment();
    private final Fragment galleryFragment = new GalleryFragment();

    BottomNavigationView.OnNavigationItemSelectedListener navigationItemSelectedListener = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            Fragment fragment = cameraFragment; //initialize with cameraFragment

            // Switch fragment based on navigation bar
            switch (item.getItemId()) {
                case R.id.action_camera:
                    fragment = cameraFragment;
                    break;
                case R.id.action_gallery:
                    fragment = galleryFragment;
                    break;
            }

            // Put fragment in container with fade in/out animation
            fragmentManager.beginTransaction()
                    .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                    .replace(R.id.fragmentContainer, fragment)
                    .commit();
            return true;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setOnNavigationItemSelectedListener(navigationItemSelectedListener);
        fragmentManager.beginTransaction().replace(R.id.fragmentContainer, cameraFragment).commit();

    }
}
