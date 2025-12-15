package com.example.mobileapp.data;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.example.mobileapp.R;
import com.example.mobileapp.model.Product;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class ProductRepository {
    private static final String TAG = "ProductRepository";

    // Demo in-memory product list (fallback if user doesn't provide assets)
    public static List<Product> loadDemoProducts(Context ctx) {
        List<Product> list = new ArrayList<>();

        // Use existing launcher drawables as placeholders
        list.add(new Product("t1", "top", R.drawable.ic_launcher_foreground, "Áo sơ mi trắng", "https://shopee.vn/item1", "Áo sơ mi trắng basic"));
        list.add(new Product("t2", "top", R.drawable.ic_launcher_foreground, "Áo thun hồng", "https://shopee.vn/item2", "Áo thun cotton"));
        list.add(new Product("b1", "bottom", R.drawable.ic_launcher_background, "Quần jean xanh", "https://shopee.vn/item3", "Quần jean rách nhẹ"));
        list.add(new Product("b2", "bottom", R.drawable.ic_launcher_background, "Quần short kaki", "https://shopee.vn/item4", "Quần short summer"));
        list.add(new Product("s1", "shoes", R.drawable.ic_launcher_foreground, "Giày trắng", "https://shopee.vn/item5", "Sneakers trắng"));
        list.add(new Product("s2", "shoes", R.drawable.ic_launcher_foreground, "Boots đen", "https://shopee.vn/item6", "Ankle boots"));

        return list;
    }

    public static List<Product> randomByCategory(List<Product> all, String category, int count) {
        List<Product> filtered = new ArrayList<>();
        for (Product p : all) if (p.category.equalsIgnoreCase(category)) filtered.add(p);
        Collections.shuffle(filtered);
        return filtered.subList(0, Math.min(count, filtered.size()));
    }

    public static List<Product> findByIds(List<Product> all, Set<String> ids) {
        List<Product> found = new ArrayList<>();
        for (Product p : all) {
            if (ids.contains(p.id)) found.add(p);
        }
        return found;
    }

    // Load products from an absolute folder path on the device (e.g., your provided path).
    // Expects subfolders 'tops', 'bottoms', 'shoes' or filenames prefixed 'top_' 'bottom_' 'shoes_'.
    public static List<Product> loadFromFolder(String absPath) {
        List<Product> list = new ArrayList<>();
        File root = new File(absPath);
        if (!root.exists() || !root.isDirectory()) {
            Log.w(TAG, "Dataset folder not found: " + absPath);
            return list;
        }

        // check subfolders
        File tops = new File(root, "tops");
        File bottoms = new File(root, "bottoms");
        File shoes = new File(root, "shoes");

        if (tops.exists() && tops.isDirectory()) addFilesFromDir(tops, "top", list);
        if (bottoms.exists() && bottoms.isDirectory()) addFilesFromDir(bottoms, "bottom", list);
        if (shoes.exists() && shoes.isDirectory()) addFilesFromDir(shoes, "shoes", list);

        // fallback: scan root and decide by filename prefix
        if (list.isEmpty()) {
            File[] files = root.listFiles();
            if (files != null) {
                for (File f : files) {
                    if (!f.isFile()) continue;
                    String name = f.getName().toLowerCase();
                    if (name.startsWith("top_") || name.contains("top")) addFile(f, "top", list);
                    else if (name.startsWith("bottom_") || name.contains("bottom") || name.contains("pant") || name.contains("quan")) addFile(f, "bottom", list);
                    else if (name.startsWith("shoe_") || name.contains("shoe") || name.contains("giay")) addFile(f, "shoes", list);
                }
            }
        }

        return list;
    }

    private static void addFilesFromDir(File dir, String category, List<Product> list) {
        File[] files = dir.listFiles();
        if (files == null) return;
        for (File f : files) {
            if (!f.isFile()) continue;
            addFile(f, category, list);
        }
    }

    private static void addFile(File f, String category, List<Product> list) {
        String id = category + "_" + System.currentTimeMillis() + "_" + Math.abs(f.getName().hashCode());
        String path = f.getAbsolutePath();
        String title = f.getName();
        list.add(new Product(id, category, path, title, "", ""));
    }

    // Attempt to load dataset from assets folder (app/src/main/assets/Clothes_Dataset)
    public static List<Product> loadFromAssets(Context ctx, String assetsFolder) {
        List<Product> list = new ArrayList<>();
        AssetManager am = ctx.getAssets();
        try {
            String[] roots = am.list(assetsFolder);
            if (roots == null) return list;
            for (String sub : roots) {
                String subPath = assetsFolder + "/" + sub;
                String[] items = am.list(subPath);
                if (items == null) continue;
                for (String item : items) {
                    String assetFile = subPath + "/" + item;
                    // We can't easily get a File for assets; store imagePath as "asset://..."
                    String imagePath = "asset://" + assetFile;
                    String id = sub + "_" + item;
                    String title = item;
                    String shopLink = "";
                    list.add(new Product(id, sub, imagePath, title, shopLink, ""));
                }
            }
        } catch (IOException e) {
            Log.w(TAG, "Error listing assets: " + e.getMessage());
        }
        return list;
    }
}
