package com.htetznaing.adbotg;

import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.BinaryMessenger;
import java.util.List;
import java.util.Map;
import android.util.Log;

/**
 * Handles method calls related to spyware detection.
 */
public class SpywareChannelHandler {
    private static final String CHANNEL = "samples.flutter.dev/spyware";

    /**
     * Sets up the method channel for handling spyware detection related calls.
     * @param messenger The binary messenger for communication.
     */
    public static void setup(BinaryMessenger messenger) {
        new MethodChannel(messenger, CHANNEL).setMethodCallHandler((call, result) -> {
            if ("getSpywareApps".equals(call.method) || "getSpywareAppsFromTarget".equals(call.method)) {
                List<List<String>> csvData = call.argument("csvData");
                boolean isTargetDevice = "getSpywareAppsFromTarget".equals(call.method);
                if (csvData != null) {
                    Log.d("SpywareChannelHandler", "Fetched CSV Data: " + csvData);
                    List<Map<String, Object>> spywareApps = SpywareDetector.getDetectedSpywareApps(csvData, isTargetDevice);
                    Log.d("SpywareChannelHandler", "Returning spyware apps: " + spywareApps);
                    result.success(spywareApps);
                } else {
                    result.error("INVALID_ARGUMENT", "CSV data is required", null);
                }
            } else {
                result.notImplemented();
            }
        });
    }
}
