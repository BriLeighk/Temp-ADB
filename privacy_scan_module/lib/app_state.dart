import 'package:flutter/services.dart';
import 'package:flutter/material.dart';

class AppState extends ChangeNotifier {
  bool _isConnected = false;
  static const platform = MethodChannel('com.htetznaing.adbotg/usb_receiver');

  AppState() {
    _initUsbConnectionListener();
  }

  bool get isConnected => _isConnected;

  void _initUsbConnectionListener() {
    platform.setMethodCallHandler((call) async {
      if (call.method == 'usbConnected') {
        _setConnectedState(true);
      } else if (call.method == 'usbDisconnected') {
        _setConnectedState(false);
      }
    });
  }

  void _setConnectedState(bool isConnected) {
    if (_isConnected != isConnected) {
      _isConnected = isConnected;
      notifyListeners();
    }
  }
}
