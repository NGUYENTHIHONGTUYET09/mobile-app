package com.example.mobileapp;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class BodyScanCameraActivity extends AppCompatActivity {

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_PICK_IMAGE = 2;
    private static final int REQUEST_CAMERA_PERMISSION = 100;

    private View cameraModeView;
    private View uploadModeView;
    private Button btnModeCamera;
    private Button btnModeUpload;
    private Button btnSelectPhoto;
    private Button btnCapturePhoto;
    private ProgressBar loadingIndicator;
    private ImageView capturedPreview;
    private TextView tvUploadError;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_body_scan_camera);

        // Find views
        cameraModeView = findViewById(R.id.camera_mode_view);
        uploadModeView = findViewById(R.id.upload_mode_view);
        btnModeCamera = findViewById(R.id.btn_mode_camera);
        btnModeUpload = findViewById(R.id.btn_mode_upload);
        btnSelectPhoto = findViewById(R.id.btn_select_photo);
        btnCapturePhoto = findViewById(R.id.btn_capture_photo);
        View loadingInclude = findViewById(R.id.loading_indicator);
        loadingIndicator = null;
        if (loadingInclude != null) {
            // the include layout root is a FrameLayout; find the ProgressBar inside it
            loadingIndicator = loadingInclude.findViewById(R.id.progress_loading);
        }
        capturedPreview = findViewById(R.id.captured_image_preview);
        tvUploadError = findViewById(R.id.tv_upload_error);

        // Default show upload mode if camera permission missing
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            showCameraMode();
        } else {
            showUploadMode();
        }

        btnModeCamera.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
            } else {
                showCameraMode();
            }
        });

        btnModeUpload.setOnClickListener(v -> showUploadMode());

        btnSelectPhoto.setOnClickListener(v -> selectPhotoFromGallery());

        btnCapturePhoto.setOnClickListener(v -> capturePhoto());

        ImageButton btnClose = findViewById(R.id.btn_close);
        btnClose.setOnClickListener(v -> finish());
    }

    private void showCameraMode() {
        if (cameraModeView != null) cameraModeView.setVisibility(View.VISIBLE);
        if (uploadModeView != null) uploadModeView.setVisibility(View.GONE);
    }

    private void showUploadMode() {
        if (cameraModeView != null) cameraModeView.setVisibility(View.GONE);
        if (uploadModeView != null) uploadModeView.setVisibility(View.VISIBLE);
    }

    private void capturePhoto() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        } else {
            Toast.makeText(this, "No camera app available", Toast.LENGTH_SHORT).show();
        }
    }

    private void selectPhotoFromGallery() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Select Photo"), REQUEST_PICK_IMAGE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                showCameraMode();
            } else {
                Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show();
                showUploadMode();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != Activity.RESULT_OK) return;

        if (requestCode == REQUEST_IMAGE_CAPTURE && data != null) {
            Bundle extras = data.getExtras();
            if (extras != null && extras.get("data") instanceof Bitmap) {
                Bitmap imageBitmap = (Bitmap) extras.get("data");
                // Show preview
                capturedPreview.setImageBitmap(imageBitmap);
                capturedPreview.setVisibility(View.VISIBLE);
                // Save thumbnail to cache and send file path
                sendBitmapToSuggestion(imageBitmap);
            }
        } else if (requestCode == REQUEST_PICK_IMAGE && data != null) {
            Uri imageUri = data.getData();
            if (imageUri != null) {
                try (InputStream is = getContentResolver().openInputStream(imageUri)) {
                    File cacheFile = saveStreamToCacheFile(is);
                    if (cacheFile != null && cacheFile.exists()) {
                        // Decode sampled bitmap for preview to avoid OOM
                        int targetW = getResources().getDisplayMetrics().widthPixels;
                        int targetH = dpToPx(300);
                        Bitmap bitmap = decodeSampledBitmapFromFile(cacheFile.getAbsolutePath(), targetW, targetH);
                        if (bitmap != null) {
                            capturedPreview.setImageBitmap(bitmap);
                            capturedPreview.setVisibility(View.VISIBLE);
                        }
                        // Send the cache file path to the suggestion activity
                        sendImageFileToSuggestion(cacheFile);
                    } else {
                        if (tvUploadError != null) {
                            tvUploadError.setText("Failed to save selected image");
                            tvUploadError.setVisibility(View.VISIBLE);
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    if (tvUploadError != null) {
                        tvUploadError.setText("Failed to load image");
                        tvUploadError.setVisibility(View.VISIBLE);
                    }
                }
            }
        }
    }

    private File saveStreamToCacheFile(InputStream is) {
        if (is == null) return null;
        File outFile = new File(getCacheDir(), "scan_" + System.currentTimeMillis() + ".jpg");
        try (FileOutputStream fos = new FileOutputStream(outFile)) {
            byte[] buffer = new byte[8192];
            int len;
            while ((len = is.read(buffer)) != -1) {
                fos.write(buffer, 0, len);
            }
            fos.flush();
            return outFile;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void sendImageFileToSuggestion(File file) {
        if (file == null) return;
        Intent i = new Intent(this, OutfitSuggestionActivity.class);
        i.putExtra("image_path", file.getAbsolutePath());
        startActivity(i);
    }

    private Bitmap decodeSampledBitmapFromFile(String path, int reqWidth, int reqHeight) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);

        int width = options.outWidth;
        int height = options.outHeight;
        if (width <= 0 || height <= 0) return null;

        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
        options.inJustDecodeBounds = false;
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;

        try {
            return BitmapFactory.decodeFile(path, options);
        } catch (OutOfMemoryError oom) {
            oom.printStackTrace();
            options.inSampleSize = Math.min(options.inSampleSize * 2, 64);
            try {
                return BitmapFactory.decodeFile(path, options);
            } catch (OutOfMemoryError oom2) {
                oom2.printStackTrace();
                return null;
            }
        }
    }

    private int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }
        return Math.max(1, inSampleSize);
    }

    private int dpToPx(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }

    private void sendBitmapToSuggestion(Bitmap bitmap) {
        // Save bitmap to cache file and pass file path to OutfitSuggestionActivity to avoid large Binder transactions
        File cacheFile = null;
        try {
            cacheFile = new File(getCacheDir(), "scan_" + System.currentTimeMillis() + ".jpg");
            try (FileOutputStream fos = new FileOutputStream(cacheFile)) {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 85, fos);
                fos.flush();
            }
        } catch (Exception e) {
            e.printStackTrace();
            cacheFile = null;
        }

        Intent i = new Intent(this, OutfitSuggestionActivity.class);
        if (cacheFile != null && cacheFile.exists()) {
            i.putExtra("image_path", cacheFile.getAbsolutePath());
        }
        startActivity(i);
    }
}
