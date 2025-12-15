package com.example.mobileapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import androidx.annotation.LayoutRes;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public abstract class BaseActivity extends AppCompatActivity {

    protected BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void setContentView(@LayoutRes int layoutResID) {
        // Inflate the base layout which contains a content frame and the bottom navigation
        super.setContentView(R.layout.activity_base);

        // Inflate the child layout into the content_frame
        LayoutInflater.from(this).inflate(layoutResID, findViewById(R.id.content_frame), true);

        // Initialize bottom navigation view from base layout
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        if (bottomNavigationView != null) {
            setupBottomNavigation();
        }
    }

    protected void setupBottomNavigation() {
        if (bottomNavigationView == null) return;

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_home) {
                if (!(this instanceof DashboardActivity)) {
                    navigateTo(DashboardActivity.class);
                }
                return true;
            } else if (itemId == R.id.nav_ai_outfit) {
                if (!(this instanceof AiOutfitActivity)) {
                    navigateTo(AiOutfitActivity.class);
                }
                return true;
            } else if (itemId == R.id.nav_wardrobe) {
                if (!(this instanceof WardrobeActivity)) {
                    navigateTo(WardrobeActivity.class);
                }
                return true;
            } else if (itemId == R.id.nav_profile) {
                // TODO: Create ProfileActivity
                return true;
            }
            return false;
        });

        // Set selected item based on current activity
        setSelectedNavItem();
    }

    private void navigateTo(Class<?> activityClass) {
        Intent intent = new Intent(this, activityClass);
        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(intent);
    }

    private void setSelectedNavItem() {
        if (bottomNavigationView == null) return;

        if (this instanceof DashboardActivity) {
            bottomNavigationView.setSelectedItemId(R.id.nav_home);
        } else if (this instanceof AiOutfitActivity) {
            bottomNavigationView.setSelectedItemId(R.id.nav_ai_outfit);
        } else if (this instanceof WardrobeActivity) {
            bottomNavigationView.setSelectedItemId(R.id.nav_wardrobe);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (bottomNavigationView != null) {
            setSelectedNavItem();
        }
    }
}
