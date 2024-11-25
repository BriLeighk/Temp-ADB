package com.htetznaing.adbotg;

import android.content.Intent;
import android.net.Uri;
import android.provider.Settings;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.BinaryMessenger;

/**
 * Handles method calls related to app settings.
 */
public class SettingsChannelHandler {
    private static final String CHANNEL = "com.example.spyware/settings";

    /**
     * Sets up the method channel for handling app settings related calls.
     * @param messenger The binary messenger for communication.
     */
    public static void setup(BinaryMessenger messenger) {
        new MethodChannel(messenger, CHANNEL).setMethodCallHandler((call, result) -> {
            if ("openAppSettings".equals(call.method)) {
                String packageName = call.argument("package");
                if (packageName != null) {
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    intent.setData(Uri.parse("package:" + packageName));
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    AppContextHolder.getContext().startActivity(intent);
                    result.success(null);
                } else {
                    result.error("ERROR", "No package name provided", null);
                }
            } else {
                result.notImplemented();
            }
        });
    }
} 