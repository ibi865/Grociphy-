package com.example.homescreen;

import android.app.Application;
import com.cloudinary.android.MediaManager;
import java.util.HashMap;
import java.util.Map;

public class MyApp extends Application {
    private static AppDatabase database;

    @Override
    public void onCreate() {
        super.onCreate();

        // Initialize Room DB
        database = AppDatabase.getDatabase(this);

        // Initialize Cloudinary
        Map<String, String> config = new HashMap<>();
        config.put("cloud_name", "di3pnpjqw");
        config.put("api_key", "215142231516394");
        config.put("api_secret", "z2qRVueEPmX_keLY2NC85vpQLe8"); // optional in dev only

        MediaManager.init(this, config);
    }

    public static AppDatabase getDatabase() {
        return database;
    }
}