import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'app_state.dart';
import 'main_page.dart';
import 'dart:async';
import 'widgets/connection_tag.dart'; // Import the ConnectionTag widget

// Main entry point of SpywareScan
Future<void> main() async {
  WidgetsFlutterBinding.ensureInitialized();
  runApp(
    ChangeNotifierProvider(
      create: (context) => AppState(),
      child: const MyApp(),
    ),
  );
}

// App Name, Color Scheme
class MyApp extends StatelessWidget {
  const MyApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Test - Spyware Detector App',
      theme: ThemeData(
        colorScheme: ColorScheme.fromSeed(
          seedColor: const Color.fromARGB(255, 84, 109, 191),
        ),
        useMaterial3: true,
      ),
      home: const MainPage(title: 'SafeScan'), // Set MainPage as the home
    );
  }
}

class HomeScreen extends StatelessWidget {
  const HomeScreen({super.key});

  @override
  Widget build(BuildContext context) {
    final appState = Provider.of<AppState>(context);

    return Scaffold(
      appBar: AppBar(
        title: Row(
          children: [
            const Text('Home'),
            if (appState.isConnected) ...[
              const SizedBox(width: 8.0),
              ConnectionTag(),
            ],
          ],
        ),
      ),
      body: const Center(child: Text('Home Screen')),
    );
  }
}

class PrivacyScanScreen extends StatelessWidget {
  const PrivacyScanScreen({super.key});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Privacy Scan')),
      body: const Center(child: Text('Privacy Scan Page')),
    );
  }
}
