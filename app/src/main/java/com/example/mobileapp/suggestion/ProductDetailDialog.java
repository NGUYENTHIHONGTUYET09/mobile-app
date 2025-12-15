package com.example.mobileapp.suggestion;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.example.mobileapp.R;
import com.example.mobileapp.model.Product;

import java.util.HashSet;
import java.util.Set;

public class ProductDetailDialog {
    public interface Callback { void onAddedToWardrobe(Product p); }

    public static Dialog create(@NonNull Context ctx, @NonNull Product p, @Nullable Callback cb) {
        AlertDialog.Builder b = new AlertDialog.Builder(ctx);
        View v = LayoutInflater.from(ctx).inflate(R.layout.dialog_product_detail, null);

        ImageView iv = v.findViewById(R.id.ivProductLarge);
        TextView tvTitle = v.findViewById(R.id.tvProductTitle);
        TextView tvDesc = v.findViewById(R.id.tvProductDesc);
        Button btnFav = v.findViewById(R.id.btnFavorite);
        Button btnShop = v.findViewById(R.id.btnShop);

        iv.setImageResource(p.imageRes);
        tvTitle.setText(p.title);
        tvDesc.setText(p.description);

        btnShop.setOnClickListener(view -> {
            if (p.shopLink != null && !p.shopLink.isEmpty()) {
                Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(p.shopLink));
                ctx.startActivity(i);
            } else {
                Toast.makeText(ctx, "Không có link mua", Toast.LENGTH_SHORT).show();
            }
        });

        btnFav.setOnClickListener(view -> {
            // save to SharedPreferences set
            Set<String> set = new HashSet<>(ctx.getSharedPreferences("FashionAssistant", Context.MODE_PRIVATE)
                    .getStringSet("wardrobe_ids", new HashSet<>()));
            set.add(p.id);
            ctx.getSharedPreferences("FashionAssistant", Context.MODE_PRIVATE).edit()
                    .putStringSet("wardrobe_ids", set).apply();

            Toast.makeText(ctx, "Đã thêm vào tủ đồ", Toast.LENGTH_SHORT).show();
            if (cb != null) cb.onAddedToWardrobe(p);
        });

        b.setView(v);
        b.setPositiveButton("Đóng", (dialog, which) -> dialog.dismiss());
        return b.create();
    }
}

