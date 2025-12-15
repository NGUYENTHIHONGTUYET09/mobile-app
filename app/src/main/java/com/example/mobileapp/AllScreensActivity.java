package com.example.mobileapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AllScreensActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_screens);

        ListView listView = findViewById(R.id.list_layouts);

        // Hardcode the layout resource names present in res/layout
        List<String> items = new ArrayList<>(Arrays.asList(
                "activity_main",
                "activity_body_scan_camera",
                "fragment_home",
                "fragment_dashboard",
                "fragment_notifications",
                "layout_camera_guide_overlay",
                "layout_error_message",
                "layout_loading_indicator"
        ));

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, items);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener((parent, view, position, id) -> {
            String name = items.get(position);
            Intent i = new Intent(AllScreensActivity.this, LayoutPreviewActivity.class);
            i.putExtra("layout_name", name);
            startActivity(i);
        });
    }
}

