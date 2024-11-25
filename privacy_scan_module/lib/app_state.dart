import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

class AppState extends ChangeNotifier {
  bool _isConnected = false;
  String _selectedDevice = 'Target'; // Default to target device
  final String _sourceDeviceName = 'Source';
  final String _targetDeviceName = 'Target';
  static const platform = MethodChannel('com.htetznaing.adbotg/usb_receiver');

  AppState() {
    _initUsbConnectionListener();
  }

  bool get isConnected => _isConnected;
  String get selectedDevice => _selectedDevice;
  String get sourceDeviceName => _sourceDeviceName;
  String get targetDeviceName => _targetDeviceName;

  void _initUsbConnectionListener() {
    platform.setMethodCallHandler((call) async {
      if (call.method == 'usbConnected') {
        _setConnectedState(true);
        selectDevice(_targetDeviceName); // Set default to target when connected
      } else if (call.method == 'usbDisconnected') {
        _setConnectedState(false);
        selectDevice(_sourceDeviceName); // Default to source when disconnected
      }
    });
  }

  void _setConnectedState(bool isConnected) {
    if (_isConnected != isConnected) {
      _isConnected = isConnected;
      notifyListeners();
    }
  }

  void selectDevice(String deviceName) {
    if (_selectedDevice != deviceName) {
      _selectedDevice = deviceName;
      notifyListeners();
    }
  }
}
