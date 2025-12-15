package com.example.mobileapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mobileapp.data.ProductRepository;
import com.example.mobileapp.model.Product;
import com.example.mobileapp.suggestion.ProductDetailDialog;
import com.example.mobileapp.suggestion.SuggestionAdapter;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class AiOutfitGeneratorActivity extends AppCompatActivity {
    private static final int PICK_IMAGE_REQUEST = 103;

    private ImageView btnBack;
    private CardView cardAddTop, cardAddBottom, cardAddShoes;
    private TextView tvHeight, tvWeight, tvSuggestion;
    private SharedPreferences prefs;
    private SkinToneClassifier classifier;
    private Bitmap bodyImage;
    private RecyclerView rvTops, rvBottoms, rvShoes;
    private List<Product> allProducts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ai_outfit_generator);

        prefs = getSharedPreferences("FashionAssistant", MODE_PRIVATE);

        try {
            classifier = new SkinToneClassifier(this);
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Lỗi khởi tạo AI model", Toast.LENGTH_SHORT).show();
        }

        initViews();
        loadUserInfo();
        setupClickListeners();

        // find recycler views
        rvTops = findViewById(R.id.rvTops);
        rvBottoms = findViewById(R.id.rvBottoms);
        rvShoes = findViewById(R.id.rvShoes);

        rvTops.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        rvBottoms.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        rvShoes.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        // Attempt to load products from assets
        allProducts = ProductRepository.loadFromAssets(this, "Clothes_Dataset");
        if (allProducts.isEmpty()) {
            // Try loading from provided absolute path (this works on emulator / device if accessible)
            String userPath = "E:/Mobile 202526 class/mobile-app/Clothes_Dataset"; // user-provided path
            allProducts = ProductRepository.loadFromFolder(userPath);
        }

        if (allProducts.isEmpty()) {
            // fallback to demo
            allProducts = ProductRepository.loadDemoProducts(this);
        }

        // If launched with an image path (from camera or gallery), load and analyze it
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("image_path")) {
            String path = intent.getStringExtra("image_path");
            if (path != null) {
                File f = new File(path);
                if (f.exists()) {
                    Bitmap bmp = BitmapFactory.decodeFile(path);
                    if (bmp != null) {
                        bodyImage = bmp;
                        analyzeBodyAndGenerateSuggestion(bodyImage);
                    } else {
                        Toast.makeText(this, "Không thể đọc ảnh", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(this, "File ảnh không tồn tại", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        cardAddTop = findViewById(R.id.cardAddTop);
        cardAddBottom = findViewById(R.id.cardAddBottom);
        tvHeight = findViewById(R.id.tvHeight);
        tvWeight = findViewById(R.id.tvWeight);
        tvSuggestion = findViewById(R.id.tvSuggestion);
    }

    private void loadUserInfo() {
        String height = prefs.getString("height", "165");
        String weight = prefs.getString("weight", "55");

        tvHeight.setText("Chiều cao\n" + height + " cm");
        tvWeight.setText("Cân nặng\n" + weight + " kg");
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());

        cardAddTop.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, PICK_IMAGE_REQUEST);
        });

        cardAddBottom.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, PICK_IMAGE_REQUEST);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            try {
                bodyImage = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                analyzeBodyAndGenerateSuggestion(bodyImage);
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Lỗi khi tải ảnh", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void showSuggestions(String skinTone) {
        Log.d("AiOutfitGenerator", "Showing suggestions using dataset size=" + (allProducts==null?0:allProducts.size()));
        // pick 4 random items each category
        List<Product> tops = ProductRepository.randomByCategory(allProducts, "top", 4);
        List<Product> bottoms = ProductRepository.randomByCategory(allProducts, "bottom", 4);
        List<Product> shoes = ProductRepository.randomByCategory(allProducts, "shoes", 4);

        SuggestionAdapter at = new SuggestionAdapter(this, tops, p -> ProductDetailDialog.create(this, p, added -> {}).show());
        SuggestionAdapter ab = new SuggestionAdapter(this, bottoms, p -> ProductDetailDialog.create(this, p, added -> {}).show());
        SuggestionAdapter as = new SuggestionAdapter(this, shoes, p -> ProductDetailDialog.create(this, p, added -> {}).show());

        rvTops.setAdapter(at);
        rvBottoms.setAdapter(ab);
        rvShoes.setAdapter(as);
    }

    private void analyzeBodyAndGenerateSuggestion(Bitmap image) {
        if (classifier == null) {
            return;
        }

        // Classify skin tone using TensorFlow Lite
        String skinTone = classifier.classifySkinTone(image);

        // Get user preferences
        String colors = prefs.getString("colors", "");
        String styles = prefs.getString("styles", "");
        String occasions = prefs.getString("occasions", "");

        // Generate outfit suggestion based on skin tone, weather, and preferences
        String suggestion = generateOutfitSuggestion(skinTone, colors, styles);

        tvSuggestion.setText(suggestion);

        // show product suggestions
        showSuggestions(skinTone);
    }

    private String generateOutfitSuggestion(String skinTone, String colors, String styles) {
        StringBuilder suggestion = new StringBuilder();
        suggestion.append("💡 Nhận vào các màu đỏ trên body đế thấy đội. AI đã tôi ưu outfit phù hợp với dáng người của bạn!\n\n");

        // Suggestion based on skin tone
        if (skinTone.toLowerCase().contains("dark") || skinTone.toLowerCase().contains("đen")) {
            suggestion.append("🎨 Làn da của bạn phù hợp với:\n");
            suggestion.append("• Màu sáng: trắng, be, hồng pastel\n");
            suggestion.append("• Màu tươi: cam, vàng, xanh lá\n");
            suggestion.append("• Tránh: màu tối quá đậm\n\n");
        } else if (skinTone.toLowerCase().contains("brown") || skinTone.toLowerCase().contains("nâu")) {
            suggestion.append("🎨 Làn da của bạn phù hợp với:\n");
            suggestion.append("• Màu trung tính: be, nâu, xanh navy\n");
            suggestion.append("• Màu ấm: cam, đỏ, vàng mù tạc\n");
            suggestion.append("• Màu tươi: xanh lá, tím\n\n");
        } else {
            suggestion.append("🎨 Làn da của bạn phù hợp với:\n");
            suggestion.append("• Màu pastel: hồng, xanh nhạt, tím nhạt\n");
            suggestion.append("• Màu trung tính: be, xám, trắng\n");
            suggestion.append("• Màu tươi: đỏ, xanh dương\n\n");
        }

        // Add style-based suggestions
        if (styles.contains("Thanh Lịch")) {
            suggestion.append("👔 Phong cách Thanh Lịch:\n");
            suggestion.append("• Áo sơ mi + quần âu\n");
            suggestion.append("• Váy midi + giày cao gót\n");
            suggestion.append("• Blazer + quần jeans\n");
        } else if (styles.contains("Năng Động")) {
            suggestion.append("🏃 Phong cách Năng Động:\n");
            suggestion.append("• Áo thun + quần jogger\n");
            suggestion.append("• Hoodie + quần short\n");
            suggestion.append("• Áo khoác bomber + sneakers\n");
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
