package com.example.mobileapp;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mobileapp.data.ProductRepository;
import com.example.mobileapp.model.Product;
import com.example.mobileapp.suggestion.SuggestionAdapter;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class WardrobeActivity extends BaseActivity {
    private RecyclerView rvWardrobe;
    private List<Product> allProducts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wardrobe);

        rvWardrobe = findViewById(R.id.rvWardrobe);
        rvWardrobe.setLayoutManager(new LinearLayoutManager(this));

        allProducts = ProductRepository.loadDemoProducts(this);

        loadWardrobeItems();

        setupBottomNavigation();
    }

    private void loadWardrobeItems() {
        SharedPreferences prefs = getSharedPreferences("FashionAssistant", MODE_PRIVATE);
        Set<String> ids = prefs.getStringSet("wardrobe_ids", new HashSet<>());
        if (ids.isEmpty()) {
            Toast.makeText(this, "Tủ đồ trống", Toast.LENGTH_SHORT).show();
            return;
        }

        List<Product> items = ProductRepository.findByIds(allProducts, ids);
        // Reuse SuggestionAdapter for simple list (it shows image+title)
        SuggestionAdapter adapter = new SuggestionAdapter(this, items, p -> {
            // noop
        });
        rvWardrobe.setAdapter(adapter);
    }
}

