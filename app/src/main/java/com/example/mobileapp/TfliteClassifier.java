package com.example.mobileapp;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
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
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

public class TfliteClassifier {
    private static final String TAG = "TfliteClassifier";
    private Interpreter interpreter;
    private List<String> labels;
    private final int inputWidth;
    private final int inputHeight;

    private TfliteClassifier(Interpreter interpreter, List<String> labels, int w, int h) {
        this.interpreter = interpreter;
        this.labels = labels;
        this.inputWidth = w;
        this.inputHeight = h;
    }

    public static TfliteClassifier createFromAsset(Context context, String modelAsset, String labelsAsset, int inputW, int inputH) throws IOException {
        MappedByteBuffer model = loadModelFile(context.getAssets(), modelAsset);
        Interpreter.Options options = new Interpreter.Options();
        options.setNumThreads(4);
        Interpreter interpreter = new Interpreter(model, options);
        List<String> labels = loadLabels(context.getAssets(), labelsAsset);
        return new TfliteClassifier(interpreter, labels, inputW, inputH);
    }

    private static MappedByteBuffer loadModelFile(AssetManager assetManager, String modelPath) throws IOException {
        AssetFileDescriptor fileDescriptor = assetManager.openFd(modelPath);
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

    private static List<String> loadLabels(AssetManager assetManager, String labelsPath) throws IOException {
        List<String> labels = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(assetManager.open(labelsPath)))) {
            String line;
            while ((line = br.readLine()) != null) labels.add(line.trim());
        }
        return labels;
    }

    public List<Recognition> classify(Bitmap bitmap, int topK) {
        if (interpreter == null) return new ArrayList<>();
        ByteBuffer input = convertBitmapToByteBuffer(bitmap, inputWidth, inputHeight);
        int numLabels = labels.size();
        float[][] output = new float[1][numLabels];
        try {
            interpreter.run(input, output);
        } catch (Exception e) {
            Log.e(TAG, "Interpreter run failed", e);
            return new ArrayList<>();
        }

        // get topK
        PriorityQueue<Recognition> pq = new PriorityQueue<>(
                topK,
                new Comparator<Recognition>() {
                    @Override
                    public int compare(Recognition a, Recognition b) {
                        return Float.compare(b.confidence, a.confidence);
                    }
                }
        );
        float[] scores = output[0];
        for (int i = 0; i < scores.length; i++) {
            pq.add(new Recognition(i, labels.get(i), scores[i]));
            if (pq.size() > topK) pq.poll();
        }
        List<Recognition> results = new ArrayList<>();
        while (!pq.isEmpty()) results.add(0, pq.poll()); // reverse order to descending
        return results;
    }

    private ByteBuffer convertBitmapToByteBuffer(Bitmap bitmap, int inputWidth, int inputHeight) {
        Bitmap resized = Bitmap.createScaledBitmap(bitmap, inputWidth, inputHeight, true);
        int bytesPerChannel = 4; // float
        ByteBuffer bb = ByteBuffer.allocateDirect(bytesPerChannel * inputWidth * inputHeight * 3);
        bb.order(ByteOrder.nativeOrder());
        int[] pixels = new int[inputWidth * inputHeight];
        resized.getPixels(pixels, 0, inputWidth, 0, 0, inputWidth, inputHeight);
        for (int p : pixels) {
            int r = (p >> 16) & 0xFF;
            int g = (p >> 8) & 0xFF;
            int b = p & 0xFF;
            // normalize to [0,1]
            bb.putFloat(r / 255.0f);
            bb.putFloat(g / 255.0f);
            bb.putFloat(b / 255.0f);
        }
        bb.rewind();
        return bb;
    }

    public void close() {
        if (interpreter != null) {
            interpreter.close();
            interpreter = null;
        }
    }

    public static class Recognition {
        public final int id;
        public final String title;
        public final float confidence;

        public Recognition(int id, String title, float confidence) {
            this.id = id;
            this.title = title;
            this.confidence = confidence;
        }
    }
}

