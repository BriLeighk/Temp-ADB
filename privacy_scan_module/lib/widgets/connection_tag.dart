import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../app_state.dart';

class ConnectionTag extends StatelessWidget {
  const ConnectionTag({super.key});

  @override
  Widget build(BuildContext context) {
    final appState = Provider.of<AppState>(context);

    if (!appState.isConnected) {
      return const SizedBox.shrink(); // Return an empty widget if not connected
    }

    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 8.0, vertical: 4.0),
      decoration: BoxDecoration(
        color: Colors.green,
        borderRadius: BorderRadius.circular(12.0),
      ),
      child: const Text(
        'Connected',
        style: TextStyle(color: Colors.white, fontSize: 12.0),
      ),
    );
  }
}
