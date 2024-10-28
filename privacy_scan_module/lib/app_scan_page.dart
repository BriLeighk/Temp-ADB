import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'app_state.dart';

class AppScanPage extends StatelessWidget {
  final bool scanTarget;

  const AppScanPage({super.key, required this.scanTarget});

  @override
  Widget build(BuildContext context) {
    final appState = Provider.of<AppState>(context);

    return Scaffold(
      appBar: AppBar(
        title: Row(
          children: [
            Text(scanTarget ? 'Target: App Scan' : 'App Scan'),
            if (appState.isConnected) ...[
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
  }
}
