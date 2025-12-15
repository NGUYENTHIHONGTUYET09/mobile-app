package com.example.mobileapp.suggestion;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mobileapp.R;
import com.example.mobileapp.model.Product;

import java.io.InputStream;
import java.util.List;

public class SuggestionAdapter extends RecyclerView.Adapter<SuggestionAdapter.VH> {
    public interface Listener { void onProductClicked(Product p); }

    private final Context ctx;
    private final List<Product> items;
    private final Listener listener;

    public SuggestionAdapter(Context ctx, List<Product> items, Listener listener) {
        this.ctx = ctx;
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(ctx).inflate(R.layout.item_suggestion, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        Product p = items.get(position);
        holder.tvTitle.setText(p.title != null ? p.title : "");

        // Load image: prefer imageRes, then asset:// path, then file path
        try {
            if (p.imageRes != null) {
                holder.iv.setImageResource(p.imageRes);
            } else if (p.imagePath != null && p.imagePath.startsWith("asset://")) {
                String assetPath = p.imagePath.substring("asset://".length());
                try (InputStream is = ctx.getAssets().open(assetPath)) {
                    Bitmap bmp = BitmapFactory.decodeStream(is);
                    holder.iv.setImageBitmap(bmp);
                }
            } else if (p.imagePath != null) {
                Bitmap bmp = BitmapFactory.decodeFile(p.imagePath);
                if (bmp != null) holder.iv.setImageBitmap(bmp);
                else holder.iv.setImageResource(R.drawable.ic_launcher_foreground);
            } else {
                holder.iv.setImageResource(R.drawable.ic_launcher_foreground);
            }
        } catch (Exception ex) {
            holder.iv.setImageResource(R.drawable.ic_launcher_foreground);
        }

        holder.itemView.setOnClickListener(v -> listener.onProductClicked(p));
    }

    @Override
    public int getItemCount() { return items.size(); }

    static class VH extends RecyclerView.ViewHolder {
        ImageView iv;
        TextView tvTitle;
        VH(@NonNull View itemView) {
            super(itemView);
            iv = itemView.findViewById(R.id.ivProduct);
            tvTitle = itemView.findViewById(R.id.tvTitle);
        }
    }
}
