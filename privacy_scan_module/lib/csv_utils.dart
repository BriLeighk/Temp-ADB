import 'dart:convert';
import 'dart:io';
import 'package:csv/csv.dart';
import 'package:flutter/services.dart' show rootBundle;
import 'package:http/http.dart' as http;

// Remote CSV URL
const String csvUrl =
    'https://raw.githubusercontent.com/stopipv/isdi/main/static_data/app-flags.csv';

// Fetch CSV data with fallback to local file
Future<List<List<dynamic>>> fetchCSVData() async {
  try {
    List<List<dynamic>> data = await fetchRemoteCSVData();
    print('Fetched remote CSV data: $data');
    return data;
  } catch (e) {
    print('Error fetching remote CSV data: $e');
    List<List<dynamic>> data = await loadLocalCSVData();
    print('Fetched local CSV data: $data');
    return data;
  }
}

// Fetch CSV data from remote URL
Future<List<List<dynamic>>> fetchRemoteCSVData() async {
  final response = await http.get(Uri.parse(csvUrl));

  if (response.statusCode == 200) {
    final rawCSVData = utf8.decode(response.bodyBytes);
    final List<List<dynamic>> csvData =
        const CsvToListConverter().convert(rawCSVData);
    print("Parsed remote CSV data: ${csvData.length} records found.");
    return csvData;
  } else {
    throw Exception(
        'Failed to load CSV data: ${response.statusCode} ${response.reasonPhrase}');
  }
}

// Load CSV data from local assets
Future<List<List<dynamic>>> loadLocalCSVData() async {
  try {
    final String csvString =
        await rootBundle.loadString('assets/app-ids-research.csv');
    final List<List<dynamic>> csvData =
        const CsvToListConverter().convert(csvString);
    print("Parsed local CSV data: ${csvData.length} records found.");
    return csvData;
  } catch (e) {
    throw Exception("Local CSV file not found or could not be read: $e");
  }
}
