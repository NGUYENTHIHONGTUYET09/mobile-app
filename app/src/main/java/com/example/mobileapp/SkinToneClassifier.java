package com.example.mobileapp;

import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.util.Log;
import org.tensorflow.lite.Interpreter;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

public class SkinToneClassifier {
    private static final String TAG = "SkinToneClassifier";
    private static final int INPUT_SIZE = 224;
    private static final int PIXEL_SIZE = 3;
    private static final int IMAGE_MEAN = 128;
    private static final float IMAGE_STD = 128.0f;

    private Interpreter interpreter;
    private List<String> labels;
    private ByteBuffer imgData;

    public SkinToneClassifier(android.content.Context context) throws IOException {
        // Load TFLite model
        interpreter = new Interpreter(loadModelFile(context));

        // Load labels
        labels = loadLabelList(context);

        // Initialize ByteBuffer for image input
        imgData = ByteBuffer.allocateDirect(
                4 * INPUT_SIZE * INPUT_SIZE * PIXEL_SIZE);
        imgData.order(ByteOrder.nativeOrder());
    }

    private MappedByteBuffer loadModelFile(android.content.Context context) throws IOException {
        AssetFileDescriptor fileDescriptor = context.getAssets().openFd("model_unquant.tflite");
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

    private List<String> loadLabelList(android.content.Context context) throws IOException {
        List<String> labels = new ArrayList<>();
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(context.getAssets().open("labels.txt")));
        String line;
        while ((line = reader.readLine()) != null) {
            labels.add(line);
        }
        reader.close();
        return labels;
    }

    public String classifySkinTone(Bitmap bitmap) {
        if (bitmap == null) {
            return "Unknown";
        }

        // Resize bitmap to model input size
        Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, INPUT_SIZE, INPUT_SIZE, true);

        // Convert bitmap to ByteBuffer
        convertBitmapToByteBuffer(scaledBitmap);

        // Run inference
        float[][] result = new float[1][labels.size()];
        interpreter.run(imgData, result);

        // Find the label with highest probability
        int maxIndex = 0;
        float maxProb = result[0][0];
        for (int i = 1; i < labels.size(); i++) {
            if (result[0][i] > maxProb) {
                maxProb = result[0][i];
                maxIndex = i;
            }
        }

        Log.d(TAG, "Classified as: " + labels.get(maxIndex) + " with probability: " + maxProb);
        return labels.get(maxIndex);
    }

    private void convertBitmapToByteBuffer(Bitmap bitmap) {
        if (imgData == null) {
            return;
        }
        imgData.rewind();

        int[] intValues = new int[INPUT_SIZE * INPUT_SIZE];
        bitmap.getPixels(intValues, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());

        int pixel = 0;
        for (int i = 0; i < INPUT_SIZE; ++i) {
            for (int j = 0; j < INPUT_SIZE; ++j) {
                final int val = intValues[pixel++];
                imgData.putFloat(((val >> 16) & 0xFF - IMAGE_MEAN) / IMAGE_STD);
                imgData.putFloat(((val >> 8) & 0xFF - IMAGE_MEAN) / IMAGE_STD);
                imgData.putFloat((val & 0xFF - IMAGE_MEAN) / IMAGE_STD);
            }
        }
    }

    public void close() {
        if (interpreter != null) {
            interpreter.close();
            interpreter = null;
        }
    }
}

