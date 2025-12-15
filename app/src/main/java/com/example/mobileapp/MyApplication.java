package com.example.mobileapp;

import android.app.Application;
import android.util.Log;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;

public class MyApplication extends Application {
    private static final String TAG = "MyApplication";

    @Override
    public void onCreate() {
        super.onCreate();

        final Thread.UncaughtExceptionHandler defaultHandler = Thread.getDefaultUncaughtExceptionHandler();

        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
            try {
                File f = new File(getCacheDir(), "crash_log.txt");
                try (PrintWriter pw = new PrintWriter(new FileWriter(f, false))) {
                    throwable.printStackTrace(pw);
                }
                Log.i(TAG, "Wrote crash log to " + f.getAbsolutePath());
            } catch (Exception e) {
                Log.e(TAG, "Failed to write crash log", e);
            }

            if (defaultHandler != null) {
                defaultHandler.uncaughtException(thread, throwable);
            } else {
                // fallback
                System.exit(2);
            }
        });
    }
}

