package com.example.mobileapp;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import java.io.IOException;

public class VirtualTryOnActivity extends AppCompatActivity {
    private static final int PICK_BODY_IMAGE = 201;
    private static final int PICK_OUTFIT_IMAGE = 202;

    private ImageView btnBack, btnRefresh;
    private CardView cardAddClothes, cardAddPants;
    private ImageView ivBodyPreview;
    private Bitmap bodyImage, outfitImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_virtual_tryon);

        btnBack = findViewById(R.id.btnBack);
        btnRefresh = findViewById(R.id.btnRefresh);
        cardAddClothes = findViewById(R.id.cardAddClothes);
        cardAddPants = findViewById(R.id.cardAddPants);
        ivBodyPreview = findViewById(R.id.ivBodyPreview);

        btnBack.setOnClickListener(v -> finish());

        btnRefresh.setOnClickListener(v -> {
            // Refresh/regenerate
            Toast.makeText(this, "Làm mới", Toast.LENGTH_SHORT).show();
        });

        cardAddClothes.setOnClickListener(v -> pickOutfitImage());

        cardAddPants.setOnClickListener(v -> pickOutfitImage());
    }

    private void pickOutfitImage() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_OUTFIT_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);

                if (requestCode == PICK_BODY_IMAGE) {
                    bodyImage = bitmap;
                    ivBodyPreview.setImageBitmap(bodyImage);
                } else if (requestCode == PICK_OUTFIT_IMAGE) {
                    outfitImage = bitmap;
                    // Process virtual try-on
                    processVirtualTryOn();
                }
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Lỗi khi tải ảnh", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void processVirtualTryOn() {
        // This would integrate with a virtual try-on API or model
        Toast.makeText(this, "Đang xử lý thử đồ ảo...", Toast.LENGTH_SHORT).show();
    }
}

