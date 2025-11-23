package com.example.umeventplanner;

import android.app.Application;
import android.text.TextUtils;
import android.util.Log;

import com.cloudinary.android.MediaManager;

import java.util.HashMap;
import java.util.Map;

public class MyApplication extends Application {

    private static final String TAG = "MyApplication";
    private static boolean isCloudinaryInitialized = false;

    @Override
    public void onCreate() {
        super.onCreate();
        initializeCloudinary();
    }

    private void initializeCloudinary() {
        if (isCloudinaryInitialized) {
            return; // Already initialized
        }

        String cloudName = BuildConfig.CLOUDINARY_CLOUD_NAME;
        String apiKey = BuildConfig.CLOUDINARY_API_KEY;
        String apiSecret = BuildConfig.CLOUDINARY_API_SECRET;

        if (TextUtils.isEmpty(cloudName) || TextUtils.isEmpty(apiKey) || TextUtils.isEmpty(apiSecret)) {
            Log.e(TAG, "FATAL ERROR: Cloudinary credentials not found. Please check your local.properties file.");
            return;
        }

        Map<String, String> config = new HashMap<>();
        config.put("cloud_name", cloudName);
        config.put("api_key", apiKey);
        config.put("api_secret", apiSecret);
        
        MediaManager.init(this, config);
        isCloudinaryInitialized = true; // Set flag after successful initialization
        Log.i(TAG, "Cloudinary SDK initialized successfully.");
    }
}
