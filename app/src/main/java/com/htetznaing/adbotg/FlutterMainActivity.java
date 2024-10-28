package com.htetznaing.adbotg;

import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbManager;
import io.flutter.embedding.android.FlutterActivity;
import io.flutter.embedding.engine.FlutterEngine;
import io.flutter.plugin.common.MethodChannel;

// Import MainActivity
import com.htetznaing.adbotg.MainActivity;

public class FlutterMainActivity extends FlutterActivity {
    private static final String CHANNEL = "com.htetznaing.adbotg/main_activity";
    private UsbReceiver usbReceiver;

    @Override
    public void configureFlutterEngine(FlutterEngine flutterEngine) {
        super.configureFlutterEngine(flutterEngine);

        usbReceiver = new UsbReceiver(flutterEngine);
        IntentFilter filter = new IntentFilter();
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        registerReceiver(usbReceiver, filter);

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
    }

    private void openMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(usbReceiver);
    }
}
