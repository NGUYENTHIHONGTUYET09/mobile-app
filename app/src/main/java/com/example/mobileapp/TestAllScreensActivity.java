package com.example.mobileapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class TestAllScreensActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ScrollView scrollView = new ScrollView(this);
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(32, 32, 32, 32);

        // Title
        TextView title = new TextView(this);
        title.setText("🧪 TEST TẤT CẢ MÀN HÌNH");
        title.setTextSize(24);
        title.setPadding(0, 0, 0, 32);
        layout.addView(title);

        // Onboarding Flow
        addSection(layout, "📝 ONBOARDING FLOW");
        addButton(layout, "1️⃣ Personal Info (Thông tin)", Step2PersonalInfoActivity.class);
        addButton(layout, "2️⃣ Preferences (Sở thích)", Step3PreferencesActivity.class);

        // Main Flow
        addSection(layout, "🏠 MAIN SCREENS");
        addButton(layout, "🏠 Dashboard (Màn hình chính)", DashboardActivity.class);

        // AI Features
        addSection(layout, "🤖 AI FEATURES");
        addButton(layout, "🎨 AI Outfit (Phân tích tông da)", AiOutfitActivity.class);
        addButton(layout, "👔 AI Outfit Generator (Tạo outfit)", AiOutfitGeneratorActivity.class);
        addButton(layout, "📸 Body Scan Camera", BodyScanCameraActivity.class);
        addButton(layout, "💡 Outfit Suggestion", OutfitSuggestionActivity.class);

        // Wardrobe
        addSection(layout, "👔 TỦ ĐỒ");
        addButton(layout, "👗 Wardrobe (Tủ đồ)", WardrobeActivity.class);

        // Virtual Try-On
        addSection(layout, "🎭 THỬ ĐỒ ẢO");
        addButton(layout, "👗 Virtual Try-On", VirtualTryOnActivity.class);

        // Old Onboarding (Deprecated)
        addSection(layout, "❌ CŨ (KHÔNG DÙNG)");
        addButton(layout, "Step1 Scan (Cũ)", Step1ScanActivity.class);
        addButton(layout, "Step2 Camera (Cũ)", Step2CameraActivity.class);
        addButton(layout, "Step1 Success (Cũ)", Step1SuccessActivity.class);

        // Dev Tools
        addSection(layout, "🔧 DEV TOOLS");
        addButton(layout, "📋 All Screens Preview", AllScreensActivity.class);
        addButton(layout, "📐 Layout Preview", LayoutPreviewActivity.class);

        scrollView.addView(layout);
        setContentView(scrollView);
    }

    private void addSection(LinearLayout layout, String title) {
        TextView section = new TextView(this);
        section.setText("\n" + title);
        section.setTextSize(18);
        section.setPadding(0, 24, 0, 16);
        section.setTypeface(null, android.graphics.Typeface.BOLD);
        layout.addView(section);
    }

    private void addButton(LinearLayout layout, String text, Class<?> activityClass) {
        Button button = new Button(this);
        button.setText(text);
        button.setAllCaps(false);
        button.setPadding(32, 24, 32, 24);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 0, 0, 16);
        button.setLayoutParams(params);

        button.setOnClickListener(v -> {
            try {
                Intent intent = new Intent(this, activityClass);
                startActivity(intent);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        layout.addView(button);
    }
}

