# SafeScan - Spyware Detection Tool

## Overview
SafeScan utilizes [ADB-OTG](https://github.com/KhunHtetzNaing/ADB-OTG) and extends its functionality to create a comprehensive spyware detection tool. The project aims to assist victims of Intimate Partner Violence (IPV) by providing a secure way to detect and manage potential spyware and privacy risks on their devices.

### Project Evolution
This repository builds upon the original [SafeScan](https://github.com/BriLeighk/SafeScan) project, which provided comprehensive app and privacy scanning for source devices. The current version extends these capabilities by:
- Integrating ADB functionality for target device scanning
- Maintaining consistent connection state across the application
- Implementing modular feature migration from the original SafeScan
- Enhancing security and reliability for dual-device operations

## Key Features

### Completed Features
- **Device Connection**
  - USB OTG connection between devices without requiring root access
  - ADB command execution from source to target device
  - Device selection dropdown for managing source/target operations

- **App Detection**
  - Scanning of installed applications against known spyware/dual-use app database
  - Risk categorization system:
    - Red: Off-store downloads (highest risk)
    - Yellow: Known spyware applications
    - Light Blue: Dual-use applications (potential for misuse)
  - Integration with remote CSV database for up-to-date threat detection

- **User Interface**
  - Clean, intuitive interface for both technical and non-technical users
  - Clear visual indicators for connection status and scan results
  - Informative permission descriptions and privacy recommendations

### In Development
+ **High Priority**
- [ ] App icon retrieval for detected applications
- [ ] Comprehensive permission analysis for detected applications
- [ ] Direct Android settings access for listed applications
- [ ] ADB connection stability improvements
- [ ] Global ADB connection state management

+ **Upcoming Features**
 - [ ] Privacy Scan implementation (migration from original SafeScan)
   - Device privacy settings analysis
   - Social media privacy recommendations
   - Google account security checkup
- [ ] Target device support for privacy scanning

+ **Note:** The Privacy Scan feature from the original SafeScan repository is currently not implemented in this version. The Privacy Scan tab exists in the interface but is non-functional until the feature migration is complete.

## Technical Improvements Over ADB-OTG
1. **Enhanced Security**
   - Improved error handling in USB connections
   - Added timeout protection for ADB operations
   - Implemented thread-safe operations

2. **Extended Functionality**
   - Added spyware detection capabilities
   - Implemented dual-device scanning
   - Integrated with external threat database

3. **Improved Architecture**
   - Modular code structure
   - Clear separation of concerns
   - Enhanced documentation

## Technology Stack
- **Core Technologies**
  - Java (Android native development)
  - ADB Protocol Implementation
  - AndroidX Libraries
  - Flutter/Dart (UI and business logic)

- **Key Components**
  - ADBLib (Modified for enhanced security)
  - USB Host API
  - Android Package Manager
  - Flutter Method Channels

## Getting Started

### Prerequisites
- Android device with USB Host support
- USB OTG cable
- Target Android device

### Installation
1. Clone the repository:
   ```bash
   git clone https://github.com/BriLeighk/SafeScan.git
   ```
2. Build the project:
   ```bash
   ./gradlew build
   ```

### Usage
1. Connect source device (with SafeScan installed) to target device via USB
2. Launch SafeScan
3. Establish ADB connection
4. Select desired scan type (App, Privacy coming soon)
5. View and analyze results

## Acknowledgments
- Original ADB-OTG project by KhunHtetzNaing
- ADB library developed by wuxudong
