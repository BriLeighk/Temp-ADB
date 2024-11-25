package com.htetznaing.adbotg;

import android.app.Application;
import android.content.Context;

/**
 * Holds the application context for global access.
 */
public class AppContextHolder extends Application {
    private static Context context;

    /**
     * Get the application context.
     * @return The application context.
     */
    public static Context getContext() {
        return context;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
    }
}
