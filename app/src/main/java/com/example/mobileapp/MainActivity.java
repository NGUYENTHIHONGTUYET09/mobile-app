package com.example.mobileapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Check if onboarding is completed
        SharedPreferences prefs = getSharedPreferences("FashionAssistant", MODE_PRIVATE);
        boolean onboardingCompleted = prefs.getBoolean("onboarding_completed", false);

        Intent intent;
        if (onboardingCompleted) {
            // Go to Dashboard
            intent = new Intent(this, DashboardActivity.class);
        } else {
            // Go to onboarding - Start with personal info
            intent = new Intent(this, Step2PersonalInfoActivity.class);
        }

        startActivity(intent);
        finish();
    }
}
