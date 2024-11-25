import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'app_state.dart';

class PrivacyScanPage extends StatelessWidget {
  final bool scanTarget;

  const PrivacyScanPage({super.key, required this.scanTarget});

  @override
  Widget build(BuildContext context) {
    final appState = Provider.of<AppState>(context);

    return Scaffold(
      appBar: AppBar(
        title: Row(
          children: [
            Text(scanTarget ? 'Target: Privacy Scan' : 'Privacy Scan'),
            const SizedBox(width: 8.0),
            if (appState.isConnected)
              DropdownButton<String>(
                value: appState.selectedDevice,
                items: [
                  DropdownMenuItem(
                    value: appState.sourceDeviceName,
                    child: Text(appState.sourceDeviceName),
                  ),
                  DropdownMenuItem(
                    value: appState.targetDeviceName,
                    child: Text(appState.targetDeviceName),
                  ),
                ],
                onChanged: (String? newValue) {
                  if (newValue != null) {
                    appState.selectDevice(newValue);
                  }
                },
              ),
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
