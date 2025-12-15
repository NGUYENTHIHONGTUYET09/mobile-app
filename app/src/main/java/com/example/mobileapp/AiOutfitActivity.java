package com.example.mobileapp;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class AiOutfitActivity extends BaseActivity {
    private ImageView ivBodyImage, btnBack;
    private Button btnCamera, btnGallery, btnAnalyze;
    private TextView tvResult;
    private Bitmap bodyImage;
    private SkinToneClassifier classifier;
    private SharedPreferences prefs;

    // Activity Result launchers
    private ActivityResultLauncher<String> requestPermissionLauncher;
    private ActivityResultLauncher<Void> takePicturePreviewLauncher;
    private ActivityResultLauncher<String> getContentLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ai_outfit);

        prefs = getSharedPreferences("FashionAssistant", MODE_PRIVATE);

        try {
            classifier = new SkinToneClassifier(this);
        } catch (IOException e) {
            Toast.makeText(this, "Lỗi khi tải AI model", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();

        // Register Activity Result launchers
        requestPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        // Permission granted, take a picture
                        takePicturePreviewLauncher.launch(null);
                    } else {
                        Toast.makeText(this, "Quyền camera bị từ chối", Toast.LENGTH_SHORT).show();
                    }
                }
        );

        takePicturePreviewLauncher = registerForActivityResult(
                new ActivityResultContracts.TakePicturePreview(),
                bitmap -> {
                    if (bitmap != null) {
                        bodyImage = bitmap;
                        ivBodyImage.setImageBitmap(bodyImage);
                        btnAnalyze.setEnabled(true);
                        tvResult.setText("");

                        // Save bitmap to cache and launch analysis activity
                        try {
                            File imageFile = new File(getCacheDir(), "captured_" + System.currentTimeMillis() + ".jpg");
                            try (FileOutputStream out = new FileOutputStream(imageFile)) {
                                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
                                out.flush();
                            }

                            Intent intent = new Intent(AiOutfitActivity.this, AiOutfitGeneratorActivity.class);
                            intent.putExtra("image_path", imageFile.getAbsolutePath());
                            startActivity(intent);
                            // Optionally finish this activity so back brings user back to generator results
                            finish();

                        } catch (IOException e) {
                            Toast.makeText(this, "Lỗi khi lưu ảnh tạm", Toast.LENGTH_SHORT).show();
                        }

                    } else {
                        Toast.makeText(this, "Không thể chụp ảnh (bitmap null)", Toast.LENGTH_SHORT).show();
                    }
                }
        );

        getContentLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        try {
                            bodyImage = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                            ivBodyImage.setImageBitmap(bodyImage);
                            btnAnalyze.setEnabled(true);
                            tvResult.setText("");

                            // Save selected image to cache and launch generator as well
                            File imageFile = new File(getCacheDir(), "selected_" + System.currentTimeMillis() + ".jpg");
                            try (FileOutputStream out = new FileOutputStream(imageFile)) {
                                bodyImage.compress(Bitmap.CompressFormat.JPEG, 90, out);
                                out.flush();
                            }

                            Intent intent = new Intent(AiOutfitActivity.this, AiOutfitGeneratorActivity.class);
                            intent.putExtra("image_path", imageFile.getAbsolutePath());
                            startActivity(intent);
                            finish();

                        } catch (IOException e) {
                            Toast.makeText(this, "Lỗi khi tải ảnh từ thư viện", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );

        setupListeners();
        setupBottomNavigation();
    }

    private void initViews() {
        ivBodyImage = findViewById(R.id.ivBodyImage);
        btnBack = findViewById(R.id.btnBack);
        btnCamera = findViewById(R.id.btnCamera);
        btnGallery = findViewById(R.id.btnGallery);
        btnAnalyze = findViewById(R.id.btnAnalyze);
        tvResult = findViewById(R.id.tvResult);

        btnAnalyze.setEnabled(false);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        btnCamera.setOnClickListener(v -> {
            // Check camera permission and request if needed
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                    == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                takePicturePreviewLauncher.launch(null);
            } else {
                requestPermissionLauncher.launch(Manifest.permission.CAMERA);
            }
        });

        btnGallery.setOnClickListener(v -> getContentLauncher.launch("image/*"));

        btnAnalyze.setOnClickListener(v -> analyzeBodyImage());
    }

    private void analyzeBodyImage() {
        if (bodyImage == null) {
            Toast.makeText(this, "Vui lòng chọn ảnh trước", Toast.LENGTH_SHORT).show();
            return;
        }

        btnAnalyze.setEnabled(false);
        tvResult.setText("Đang phân tích...");

        new Thread(() -> {
            String skinTone = classifier.classifySkinTone(bodyImage);
            String suggestion = generateOutfitSuggestion(skinTone);

            runOnUiThread(() -> {
                tvResult.setText(suggestion);
                btnAnalyze.setEnabled(true);
            });
        }).start();
    }

    private String generateOutfitSuggestion(String skinTone) {
        String styles = prefs.getString("styles", "Thanh Lịch");
        String colors = prefs.getString("colors", "");
        String occasions = prefs.getString("occasions", "");

        StringBuilder suggestion = new StringBuilder();
        suggestion.append("🎨 Phân tích màu da: ").append(skinTone).append("\n\n");

        // Gợi ý dựa trên màu da
        if (skinTone.toLowerCase().contains("light") || skinTone.toLowerCase().contains("fair")) {
            suggestion.append("✨ Với tone da sáng của bạn:\n");
            suggestion.append("• Màu pastel và màu tươi sáng sẽ làm bạn nổi bật\n");
            suggestion.append("• Tránh màu quá nhạt có thể làm bạn kém sắc\n");
            suggestion.append("• Thử các màu: hồng đào, xanh navy, đỏ burgundy\n\n");
        } else if (skinTone.toLowerCase().contains("medium") || skinTone.toLowerCase().contains("tan")) {
            suggestion.append("✨ Với tone da vừa của bạn:\n");
            suggestion.append("• Bạn phù hợp với hầu hết các màu sắc\n");
            suggestion.append("• Màu đất và màu ấm sẽ tôn lên vẻ đẹp tự nhiên\n");
            suggestion.append("• Thử các màu: cam đất, xanh olive, vàng mù tạt\n\n");
        } else {
            suggestion.append("✨ Với tone da ngăm của bạn:\n");
            suggestion.append("• Màu sáng và màu neon sẽ làm bạn nổi bật\n");
            suggestion.append("• Màu tím, xanh lá và vàng rất phù hợp\n");
            suggestion.append("• Thử các màu: trắng tinh khôi, vàng chanh, tím lavender\n\n");
        }

        // Gợi ý theo phong cách
        suggestion.append("👗 Gợi ý outfit theo phong cách của bạn:\n");
        if (styles.contains("Thanh Lịch")) {
            suggestion.append("• Áo sơ mi trắng + Quần âu + Giày cao gót\n");
            suggestion.append("• Váy midi + Áo blazer + Túi xách structured\n");
        } else if (styles.contains("Năng Động")) {
            suggestion.append("• Áo thun + Quần jeans + Sneakers\n");
            suggestion.append("• Jumpsuit + Giày thể thao + Ba lô\n");
        } else if (styles.contains("Cá Tính")) {
            suggestion.append("• Áo phông oversized + Quần rách + Boots\n");
            suggestion.append("• Leather jacket + Váy ngắn + Ankle boots\n");
        }

        return suggestion.toString();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (classifier != null) {
            classifier.close();
        }
    }
}
