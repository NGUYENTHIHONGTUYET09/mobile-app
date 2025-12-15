package com.example.mobileapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import java.util.ArrayList;
import java.util.List;

public class Step3PreferencesActivity extends AppCompatActivity {
    private GridLayout colorGrid;
    private ChipGroup chipGroupOccasions, chipGroupStyles;
    private Button btnComplete;
    private SharedPreferences prefs;
    private List<String> selectedColors = new ArrayList<>();

    // Color data
    private String[] colorNames = {"Đỏ", "Hồng", "Cam", "Vàng", "Xanh Lá",
                                    "Xanh Dương", "Tím", "Trắng", "Đen", "Be"};
    private String[] colorHexValues = {"#FF6B8E", "#FF69B4", "#FFA500", "#FFD700", "#00FF7F",
                                       "#4A90E2", "#9B59B6", "#FFFFFF", "#000000", "#F5E6D3"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_step3_preferences);

        prefs = getSharedPreferences("FashionAssistant", MODE_PRIVATE);

        colorGrid = findViewById(R.id.colorGrid);
        chipGroupOccasions = findViewById(R.id.chipGroupOccasions);
        chipGroupStyles = findViewById(R.id.chipGroupStyles);
        btnComplete = findViewById(R.id.btnComplete);

        setupColorGrid();
        btnComplete.setOnClickListener(v -> saveAndComplete());
    }

    private void setupColorGrid() {
        for (int i = 0; i < colorNames.length; i++) {
            View colorView = new View(this);
            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = 120;
            params.height = 120;
            params.setMargins(12, 12, 12, 12);
            colorView.setLayoutParams(params);

            final String colorName = colorNames[i];
            final String colorHex = colorHexValues[i];

            colorView.setBackgroundColor(Color.parseColor(colorHex));

            // Add border
            if (colorHex.equals("#FFFFFF")) {
                colorView.setBackground(getDrawable(R.drawable.bg_color_choice));
            }

            final int index = i;
            colorView.setOnClickListener(v -> {
                if (selectedColors.contains(colorName)) {
                    selectedColors.remove(colorName);
                    colorView.setAlpha(1.0f);
                } else {
                    selectedColors.add(colorName);
                    colorView.setAlpha(0.5f);
                }
            });

            colorGrid.addView(colorView);
        }
    }

    private void saveAndComplete() {
        // Get selected occasions
        List<String> occasions = new ArrayList<>();
        for (int i = 0; i < chipGroupOccasions.getChildCount(); i++) {
            Chip chip = (Chip) chipGroupOccasions.getChildAt(i);
            if (chip.isChecked()) {
                occasions.add(chip.getText().toString());
            }
        }

        // Get selected styles
        List<String> styles = new ArrayList<>();
        for (int i = 0; i < chipGroupStyles.getChildCount(); i++) {
            Chip chip = (Chip) chipGroupStyles.getChildAt(i);
            if (chip.isChecked()) {
                styles.add(chip.getText().toString());
            }
        }

        if (selectedColors.isEmpty() || occasions.isEmpty() || styles.isEmpty()) {
            Toast.makeText(this, "Vui lòng chọn ít nhất một mục cho mỗi danh mục",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        // Save preferences
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("colors", String.join(",", selectedColors));
        editor.putString("occasions", String.join(",", occasions));
        editor.putString("styles", String.join(",", styles));
        editor.putBoolean("onboarding_completed", true);
        editor.apply();

        // Go to Dashboard
        Intent intent = new Intent(this, DashboardActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}

