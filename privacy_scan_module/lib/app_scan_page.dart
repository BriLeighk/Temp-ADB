import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

class AppScanPage extends StatelessWidget {
  final bool scanTarget;

  const AppScanPage({super.key, required this.scanTarget});

  static const platform = MethodChannel('com.htetznaing.adbotg/main_activity');

  Future<bool> _checkConnectionStatus() async {
    try {
      final bool result = await platform.invokeMethod('isDeviceConnected');
      return result;
    } on PlatformException catch (e) {
      print("Failed to get connection status: '${e.message}'.");
      return false;
    }
  }

  @override
  Widget build(BuildContext context) {
    return FutureBuilder<bool>(
      future: _checkConnectionStatus(),
      builder: (context, snapshot) {
        final isConnected = snapshot.data ?? false;

        return Scaffold(
          appBar: AppBar(
            title: Row(
              children: [
                Text(scanTarget ? 'Target: App Scan' : 'App Scan'),
                if (isConnected) ...[
                  const SizedBox(width: 8.0),
                  const Icon(Icons.check_circle, color: Colors.green),
                ],
              ],
            ),
          ),
          body: Center(
            child: Text(
              'App Scan Page for ${scanTarget ? "Target" : "Source"} Device',
              style: const TextStyle(fontSize: 18),
            ),
          ),
        );
      },
    );
  }
}
