package com.htetznaing.adbotg;

import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.BinaryMessenger;
import java.util.Map;

/**
 * Handles method calls related to fetching app details.
 * 
 * Currently not in use, but setup in case the AppDetailsFetcher
 * needs to be invoked directly, not just through SpywareDetector.java
 */
public class AppDetailsChannelHandler {
    private static final String CHANNEL = "com.htetznaing.adbotg/app_details";

    /**
     * Sets up the method channel for handling app details related calls.
     * @param messenger The binary messenger for communication.
     */
    public static void setup(BinaryMessenger messenger) {
        new MethodChannel(messenger, CHANNEL).setMethodCallHandler((call, result) -> {
            if ("getAppDetails".equals(call.method)) {
                String packageName = call.argument("packageName");
                if (packageName != null) {
                    Map<String, String> appDetails = AppDetailsFetcher.getAppDetails(packageName);
                    result.success(appDetails);
                } else {
                    result.error("INVALID_PACKAGE", "Package name is required", null);
                }
            } else {
                result.notImplemented();
            }
        });
    }
}
