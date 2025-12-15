package com.example.mobileapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class Step1ScanActivity extends AppCompatActivity {
    private Button btnStartScan;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_step1_scan);

        btnStartScan = findViewById(R.id.btnStartScan);
        btnStartScan.setOnClickListener(v -> {
            // Navigate to camera activity to scan body
            Intent intent = new Intent(this, Step2CameraActivity.class);
            startActivity(intent);
        });
    }
}

