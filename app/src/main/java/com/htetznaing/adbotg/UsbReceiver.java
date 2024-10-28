package com.htetznaing.adbotg;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbManager;
import io.flutter.embedding.engine.FlutterEngine;
import io.flutter.plugin.common.MethodChannel;

public class UsbReceiver extends BroadcastReceiver {
    private static final String CHANNEL = "com.htetznaing.adbotg/usb_receiver";
    private final FlutterEngine flutterEngine;

    public UsbReceiver(FlutterEngine flutterEngine) {
        this.flutterEngine = flutterEngine;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action;
        if (intent != null && (action = intent.getAction()) != null) {
            if (action.equals(UsbManager.ACTION_USB_DEVICE_ATTACHED)) {
                // Notify Flutter about USB connection
                new MethodChannel(flutterEngine.getDartExecutor().getBinaryMessenger(), CHANNEL)
                        .invokeMethod("usbConnected", null);
            } else if (action.equals(UsbManager.ACTION_USB_DEVICE_DETACHED)) {
                // Notify Flutter about USB disconnection
                new MethodChannel(flutterEngine.getDartExecutor().getBinaryMessenger(), CHANNEL)
                        .invokeMethod("usbDisconnected", null);
            }
        }
    }
}
