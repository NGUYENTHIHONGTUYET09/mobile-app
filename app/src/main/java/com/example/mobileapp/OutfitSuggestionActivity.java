package com.example.mobileapp;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.util.List;

public class OutfitSuggestionActivity extends AppCompatActivity {

    private static final String TAG = "OutfitSuggestionActivity";
    private TfliteClassifier classifier = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_outfit_suggestion);

            ImageView ivPreview = findViewById(R.id.iv_preview);
            LinearLayout suggestionsContainer = findViewById(R.id.suggestions_container);
            Button btnDone = findViewById(R.id.btn_done);

            String imagePath = getIntent().getStringExtra("image_path");
            if (imagePath != null) {
                File f = new File(imagePath);
                if (f.exists()) {
                    Log.i(TAG, "Loading image from path: " + imagePath);
                    int targetW = getResources().getDisplayMetrics().widthPixels;
                    int targetH = dpToPx(400);
                    Bitmap bmp = decodeSampledBitmapFromFile(imagePath, targetW, targetH);
                    if (bmp != null) {
                        ivPreview.setImageBitmap(bmp);
                        // Load TFLite classifier and run inference in background
                        new Thread(() -> {
                            try {
                                if (classifier == null) {
                                    classifier = TfliteClassifier.createFromAsset(this, "model_unquant.tflite", "labels.txt", 224, 224);
                                }
                                List<TfliteClassifier.Recognition> results = classifier.classify(bmp, 3);
                                runOnUiThread(() -> {
                                    if (results != null && !results.isEmpty()) {
                                        // top label -> attempt to map to skin tone
                                        String topLabel = results.get(0).title != null ? results.get(0).title.toLowerCase() : "";
                                        String tone = mapLabelToSkinTone(topLabel);
                                        String season = getSeason();
                                        // build tailored suggestions based on tone+season
                                        List<String> tailored = buildSuggestionsForToneSeason(tone, season);
                                        if (tailored != null && !tailored.isEmpty()) {
                                            for (int i = tailored.size() - 1; i >= 0; i--) {
                                                String s = tailored.get(i);
                                                TextView tv = new TextView(OutfitSuggestionActivity.this);
                                                tv.setText(s);
                                                tv.setTextSize(16);
                                                tv.setPadding(dpToPx(8), dpToPx(8), dpToPx(8), dpToPx(8));
                                                // insert at top
                                                suggestionsContainer.addView(tv, 0);
                                            }
                                        } else {
                                            // fallback: show classifier results textually
                                            for (TfliteClassifier.Recognition r : results) {
                                                TextView tv = new TextView(OutfitSuggestionActivity.this);
                                                tv.setText(String.format("%s (%.2f)", r.title, r.confidence));
                                                tv.setTextSize(16);
                                                tv.setPadding(dpToPx(8), dpToPx(8), dpToPx(8), dpToPx(8));
                                                suggestionsContainer.addView(tv, 0);
                                            }
                                        }
                                    }
                                });
                            } catch (Exception e) {
                                Log.e(TAG, "Classifier failed", e);
                            }
                        }).start();
                    } else {
                        Log.w(TAG, "Decoded bitmap is null for path=" + imagePath);
                        ivPreview.setImageResource(android.R.color.darker_gray);
                    }
                } else {
                    Log.w(TAG, "Image file does not exist: " + imagePath);
                }
            } else {
                Log.i(TAG, "No image_path extra provided");
            }

            // Mock suggestions
            String[] suggestions = new String[] {
                    "Try a slim-fit denim jacket",
                    "Wear a white tee with tailored trousers",
                    "Add a belt for waist definition",
                    "Choose neutral-toned shoes"
            };

            for (String s : suggestions) {
                TextView tv = new TextView(this);
                tv.setText(s);
                tv.setTextSize(16);
                int pad = dpToPx(8);
                tv.setPadding(pad, pad, pad, pad);
                tv.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                suggestionsContainer.addView(tv);
            }

            btnDone.setOnClickListener(v -> finish());

        } catch (Exception e) {
            Log.e(TAG, "Exception in OutfitSuggestionActivity", e);
            TextView tv = new TextView(this);
            tv.setText("An error occurred: " + e.getClass().getSimpleName() + " - " + e.getMessage());
            tv.setPadding(dpToPx(16), dpToPx(16), dpToPx(16), dpToPx(16));
            setContentView(tv);
        }
    }

    // Map top label string to one of: "dark", "brown", "fair"; returns "unknown" if no match.
    private String mapLabelToSkinTone(String labelLower) {
        if (labelLower == null) return "unknown";
        if (labelLower.contains("dark") || labelLower.contains("đen") || labelLower.contains("den")) return "dark";
        if (labelLower.contains("brown") || labelLower.contains("nau") || labelLower.contains("nâu")) return "brown";
        if (labelLower.contains("fair") || labelLower.contains("white") || labelLower.contains("trang") || labelLower.contains("pink") || labelLower.contains("hồng") || labelLower.contains("hong")) return "fair";
        return "unknown";
    }

    // Determine season by current month (Northern Hemisphere): Dec-Feb=winter, Mar-May=spring, Jun-Aug=summer, Sep-Nov=autumn
    private String getSeason() {
        java.util.Calendar c = java.util.Calendar.getInstance();
        int m = c.get(java.util.Calendar.MONTH) + 1; // months 1..12
        if (m == 12 || m == 1 || m == 2) return "winter";
        if (m >= 3 && m <= 5) return "spring";
        if (m >= 6 && m <= 8) return "summer";
        return "autumn";
    }

    private java.util.List<String> buildSuggestionsForToneSeason(String tone, String season) {
        java.util.List<String> out = new java.util.ArrayList<>();
        // Example rules (customize later). These are text-only guidance.
        switch (tone) {
            case "dark":
                if ("summer".equals(season)) {
                    out.add("Light, breathable colors (ivory, soft pastels) to contrast your skin tone");
                    out.add("Avoid very dark head-to-toe outfits; add a bright accessory");
                } else if ("winter".equals(season)) {
                    out.add("Bold jewel tones (royal blue, emerald) look great in colder months");
                    out.add("Layer with textured coats for depth");
                } else if ("spring".equals(season)) {
                    out.add("Earthy mid-tones and warm pastels complement darker skin");
                    out.add("Light layers: bomber or denim jacket");
                } else { // autumn
                    out.add("Warm autumn palette: burnt orange, mustard, olive");
                    out.add("Pair with neutral boots and textured fabrics");
                }
                break;
            case "brown":
                if ("summer".equals(season)) {
                    out.add("Warm neutrals and coral shades brighten brown skin in summer");
                    out.add("Choose cottons and linens for comfort");
                } else if ("winter".equals(season)) {
                    out.add("Deep blues and burgundy create a refined winter look");
                    out.add("Add scarves or hats in complementary colors");
                } else if ("spring".equals(season)) {
                    out.add("Pastels with warm undertones (peach, light mint) work well");
                    out.add("Light blazers or cardigans for layering");
                } else {
                    out.add("Autumnal colors (olive, rust, camel) harmonize beautifully");
                    out.add("Wear suede or leather accents for texture");
                }
                break;
            case "fair":
                if ("summer".equals(season)) {
                    out.add("Vibrant colors (turquoise, coral) add life to fair skin in summer");
                    out.add("Avoid washed-out beiges; pick saturated tones");
                } else if ("winter".equals(season)) {
                    out.add("Cool tones like navy and charcoal create a crisp look");
                    out.add("Layer with structured coats and statement scarves");
                } else if ("spring".equals(season)) {
                    out.add("Light florals and soft colors (lavender, peach) complement fair skin");
                    out.add("Try lightweight dresses or chinos for a fresh look");
                } else {
                    out.add("Warm camel, soft browns and muted greens suit autumn");
                    out.add("Add textured knits to balance pale tones");
                }
                break;
            default:
                // unknown: provide general seasonal tips
                if ("summer".equals(season)) {
                    out.add("Choose breathable fabrics (linen, cotton) and lighter colors for summer");
                } else if ("winter".equals(season)) {
                    out.add("Layer warmly: coats, scarves, and boots for winter");
                } else if ("spring".equals(season)) {
                    out.add("Light layers and brighter colors fit spring");
                } else {
                    out.add("Autumn suggests layering with warm textures and earthy tones");
                }
                break;
        }
        return out;
    }

    private int dpToPx(int dp) {
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        return Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
    }

    private Bitmap decodeSampledBitmapFromFile(String path, int reqWidth, int reqHeight) {
        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(path, options);
    }

    private int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }
}
