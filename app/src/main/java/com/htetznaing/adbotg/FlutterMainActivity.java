package com.htetznaing.adbotg;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbManager;
import androidx.core.content.ContextCompat;
import io.flutter.embedding.android.FlutterActivity;
import io.flutter.embedding.engine.FlutterEngine;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.BinaryMessenger;
import java.util.List;
import java.util.Map;

// Import MainActivity
import com.htetznaing.adbotg.MainActivity;

/**
 * Main activity for the Flutter application.
 * Handles method channels for various functionalities.
 */
public class FlutterMainActivity extends FlutterActivity {
    private static final String CHANNEL = "com.htetznaing.adbotg/main_activity";
    private static final String USB_CHANNEL = "com.htetznaing.adbotg/usb_receiver";
    private static final String SPYWARE_CHANNEL = "samples.flutter.dev/spyware";
    private static final String APP_DETAILS_CHANNEL = "com.htetznaing.adbotg/app_details";
    private MethodChannel methodChannel;

    @Override
    public void configureFlutterEngine(FlutterEngine flutterEngine) {
        super.configureFlutterEngine(flutterEngine);

        // Setup method channel for opening main activity
        new MethodChannel(flutterEngine.getDartExecutor().getBinaryMessenger(), CHANNEL)
            .setMethodCallHandler(
                (call, result) -> {
                    if (call.method.equals("openMainActivity")) {
                        openMainActivity();
                        result.success(null);
                    } else {
                        result.notImplemented();
                    }
                }
            );

        // Setup AppDetailsChannelHandler
        AppDetailsChannelHandler.setup(flutterEngine.getDartExecutor().getBinaryMessenger());

        // Listen for USB connection status
        methodChannel = new MethodChannel(flutterEngine.getDartExecutor().getBinaryMessenger(), USB_CHANNEL);

        // Register the broadcast receiver
        IntentFilter filter = new IntentFilter("com.htetznaing.adbotg.USB_STATUS");
        registerReceiver(usbStatusReceiver, filter);

        // Setup spyware channel
        SpywareChannelHandler.setup(flutterEngine.getDartExecutor().getBinaryMessenger());

        new MethodChannel(flutterEngine.getDartExecutor().getBinaryMessenger(), SPYWARE_CHANNEL)
            .setMethodCallHandler(
                (call, result) -> {
                    if ("getSpywareApps".equals(call.method)) {
                        List<List<String>> csvData = call.argument("csvData");
                        if (csvData != null) {
                            List<Map<String, Object>> spywareApps = SpywareDetector.getDetectedSpywareApps(csvData, false);
                            result.success(spywareApps);
                        } else {
                            result.error("INVALID_ARGUMENT", "CSV data is required", null);
                        }
                    } else if ("getSpywareAppsFromTarget".equals(call.method)) {
                        List<List<String>> csvData = call.argument("csvData");
                        if (csvData != null) {
                            List<Map<String, Object>> spywareApps = SpywareDetector.getDetectedSpywareApps(csvData, true);
                            result.success(spywareApps);
                        } else {
                            result.error("INVALID_ARGUMENT", "CSV data is required", null);
                        }
                    } else {
                        result.notImplemented();
                    }
                }
            );
    }

    /**
     * Opens the main activity.
     */
    private void openMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    /**
     * Broadcast receiver for USB connection status.
     */
    private final BroadcastReceiver usbStatusReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            boolean isConnected = intent.getBooleanExtra("isConnected", false);
            methodChannel.invokeMethod(isConnected ? "usbConnected" : "usbDisconnected", null);
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(usbStatusReceiver);
    }
}
