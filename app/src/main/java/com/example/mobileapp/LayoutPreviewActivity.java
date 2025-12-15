package com.example.mobileapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class LayoutPreviewActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Create root layout programmatically
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        TextView title = new TextView(this);
        title.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        FrameLayout container = new FrameLayout(this);
        container.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));

        root.addView(title);
        root.addView(container);

        setContentView(root);

        String layoutName = getIntent().getStringExtra("layout_name");
        if (layoutName == null) layoutName = "unknown";
        title.setText(layoutName);

        int resId = getResources().getIdentifier(layoutName, "layout", getPackageName());
        if (resId != 0) {
            try {
                LayoutInflater.from(this).inflate(resId, container, true);
            } catch (Exception e) {
                TextView tv = new TextView(this);
                tv.setText("Failed to inflate layout: " + e.getMessage());
                container.addView(tv);
            }
        } else {
            TextView tv = new TextView(this);
            tv.setText("Layout not found: " + layoutName);
            container.addView(tv);
        }
    }
}
