import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'app_state.dart';
import 'widgets/connection_tag.dart';
import 'package:provider/provider.dart';

class PrivacyScanPage extends StatelessWidget {
  final bool scanTarget;

  const PrivacyScanPage({super.key, required this.scanTarget});

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
    final appState = Provider.of<AppState>(context);

    // Check connection status and update UI
    appState.updateConnectionStatus(context, appState.isConnected);

    return Scaffold(
      appBar: AppBar(
        title: Row(
          children: [
            Text(scanTarget ? 'Target: Privacy Scan' : 'Privacy Scan'),
            if (appState.isConnected) ...[
              const SizedBox(width: 8.0),
              const Icon(Icons.check_circle, color: Colors.green),
            ],
          ],
        ),
      ),
      body: Center(
        child: Text(
          'Privacy Scan Page for ${scanTarget ? "Target" : "Source"} Device',
          style: const TextStyle(fontSize: 18),
        ),
      ),
    );
  }
}
